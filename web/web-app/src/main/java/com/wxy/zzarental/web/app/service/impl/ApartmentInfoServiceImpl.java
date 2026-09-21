package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.util.RedisKeyUtil;
import com.wxy.zzarental.common.util.RedisUtil;
import com.wxy.zzarental.web.app.entity.*;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.dto.ApartmentDetailDTO;
import com.wxy.zzarental.web.app.service.dto.ApartmentItemDTO;
import com.wxy.zzarental.web.app.service.dto.GraphDTO;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Resource
    private ApartmentLabelMapper apartmentLabelMapper;
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private LabelInfoMapper labelInfoMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Override
    public ApartmentItemDTO getInfoById(Long apartmentId) {
        ApartmentInfo apartmentInfo = getById(apartmentId);
        if (apartmentInfo == null) {
            return null;
        }
        ApartmentItemDTO apartmentItemVo = new ApartmentItemDTO();
        BeanUtil.copyProperties(apartmentInfo, apartmentItemVo);
        //公寓的labelInfoList，graphVoList，minRent
        LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType, 1);
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemId, apartmentInfo.getId());
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphVoLambdaQueryWrapper);
        List<GraphDTO> graphVos = graphInfos.stream().map(
                graphInfo -> {
                    GraphDTO graphVo = new GraphDTO();
                    graphVo.setName(graphInfo.getName());
                    graphVo.setUrl(graphInfo.getUrl());
                    return graphVo;
                }).toList();
        apartmentItemVo.setGraphVoList(graphVos);

        //公寓标签
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentInfo.getId());
        List<ApartmentLabel> apartmentLabels = apartmentLabelMapper.selectList(apartmentLabelLambdaQueryWrapper);
        Set<Long> labelIds = apartmentLabels.stream().map(ApartmentLabel::getLabelId).collect(Collectors.toSet());
        List<LabelInfo> labelInfos = labelInfoMapper.selectBatchIds(labelIds);
        apartmentItemVo.setLabelInfoList(labelInfos);

        //最低租金
        BigDecimal minRent = roomInfoMapper.selectMinRent(apartmentInfo.getId());
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }

    @Resource
    private ApartmentFacilityMapper apartmentFacilityMapper;
    @Resource
    private FacilityInfoMapper facilityInfoMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private RedissonClient redissonClient;


    // setnx == 1
    // 删除缓存,这个删除缓存是在下架公寓的接口里面s
    // 下架公寓
    // 删除缓存
    // del key
    //你这里删除key，那现在没有key，现在的setnx =1

    @Override
    public ApartmentDetailDTO getDetailById(Long id) {
        // setnx == 0，此时往rdis插入失败，代表下架公寓接口正在执行，所以我们接口需要等待Thread.sleep(1);
        // 你说的好乱啊，究竟这里要什么条件才走，等于1走
//你这里说要满足 ==0 又说==1 才执行，啥意思啊，，那你怎么怎么可能等到==1，你不是说注释赋值吗，你现在让setnx等于1，那你判断啥啊在
        //
        String js = redisUtil.get(RedisKeyUtil.getApartmentKey(id));
        if (StrUtil.isNotBlank(js)){
            Gson gson = new Gson();
            return gson.fromJson(js,ApartmentDetailDTO.class);

        }

        String key = RedisKeyUtil.getApartmentLockKey(id);
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(key);
        RLock rLock = readWriteLock.readLock();


        try {
            boolean b = rLock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!b){
                throw new ZZAException(1002,"sad");
            }

            ApartmentItemDTO apartmentItemVo = getInfoById(id);
            if (apartmentItemVo == null) {
                redisUtil.set(RedisKeyUtil.getApartmentKey(id),null,60*60+ RandomUtil.randomInt(20,200),TimeUnit.SECONDS);
                return null;
            }
            ApartmentDetailDTO apartmentDetailVo = new ApartmentDetailDTO();
            BeanUtil.copyProperties(apartmentItemVo,apartmentDetailVo);
            //得到配套信息列表List<FacilityInfo> facilityInfoList;
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
            List<ApartmentFacility> apartmentFacilities = apartmentFacilityMapper.selectList(apartmentFacilityLambdaQueryWrapper);
            List<Long> facilityIds = apartmentFacilities.stream().map(ApartmentFacility::getFacilityId).toList();

            LambdaQueryWrapper<FacilityInfo> facilityInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            facilityInfoLambdaQueryWrapper.in(BaseEntity::getId,facilityIds);
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectList(facilityInfoLambdaQueryWrapper);


            apartmentDetailVo.setFacilityInfoList(facilityInfoList);
            Gson gson = new Gson();
            String json = gson.toJson(apartmentDetailVo);

            redisUtil.set(RedisKeyUtil.getApartmentKey(id),json,60*60+ RandomUtil.randomInt(20,100), TimeUnit.SECONDS);
            return apartmentDetailVo;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }
}




