package com.wxy.zzarental.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.mapper.*;
import com.wxy.zzarental.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentItemVo;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentQueryVo;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentSubmitVo;
import com.wxy.zzarental.web.admin.vo.graph.GraphVo;
import jakarta.annotation.Resource;
import okhttp3.Cookie;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private GraphInfoService graphInfoService;
    @Resource
    private ApartmentFacilityService apartmentFacilityService;
    @Resource
    private ApartmentLabelService apartmentLabelService;
    @Resource
    private ApartmentFeeValueService apartmentFeeValueService;
    @Resource
    private RoomInfoService roomInfoService;
    @Resource
    private LeaseAgreementService leaseAgreementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateApart(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate = apartmentSubmitVo.getId() != null;
        ApartmentInfo apartmentInfo = new ApartmentInfo();
        // 复制属性
        BeanUtils.copyProperties(apartmentSubmitVo, apartmentInfo);
        saveOrUpdate(apartmentInfo);
        if(isUpdate){
            LambdaQueryWrapper<GraphInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            queryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoService.remove(queryWrapper);

            // 查询配套设施关联表，删除关联的设施记录
            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);

            //标签关联表，删除关联的标签记录
            LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelQueryWrapper);
            //杂费值关联表，删除关联的杂费值记录
            LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
            feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeQueryWrapper);
        }
        //新增图片
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        List<GraphInfo> graphInfoList = graphVoList.stream().map(item -> {
            GraphInfo graphInfo = new GraphInfo();
            graphInfo.setName(item.getName());
            graphInfo.setUrl(item.getUrl());
            graphInfo.setItemType(ItemType.APARTMENT);
            graphInfo.setItemId(apartmentSubmitVo.getId());
            return graphInfo;
        }).toList();
        graphInfoService.saveBatch(graphInfoList);

        //新增设施
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        List<ApartmentFacility> facilityList = facilityInfoIds.stream().map(item -> {
            return ApartmentFacility.builder().apartmentId(apartmentSubmitVo.getId()).facilityId(item).build();
        }).toList();
        apartmentFacilityService.saveBatch(facilityList);

        //新增标签
        List<Long> labelInfoIds = apartmentSubmitVo.getLabelIds();
        List<ApartmentLabel> labelList = labelInfoIds.stream().map(item -> {
            return ApartmentLabel.builder().apartmentId(apartmentSubmitVo.getId()).labelId(item).build();
        }).toList();
        apartmentLabelService.saveBatch(labelList);

        //新增杂费值
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        List<ApartmentFeeValue> feeValueList = feeValueIds.stream()
                .map(item -> ApartmentFeeValue.builder()
                        .apartmentId(apartmentSubmitVo.getId()).feeValueId(item).build())
                .toList();
        apartmentFeeValueService.saveBatch(feeValueList);
    }


    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        IPage<ApartmentItemVo> result = apartmentInfoMapper.pageItem(page, queryVo);
        List<ApartmentItemVo> records = result.getRecords();
        // 获取公寓id
        List<Long> idList = records.stream().map(ApartmentInfo::getId).toList();
        // 根据公寓id列表查询房间列表
        LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
        roomQueryWrapper.in(RoomInfo::getApartmentId, idList);
        List<RoomInfo> roomList = roomInfoService.list(roomQueryWrapper);

        // 将房间列表转换为Map<公寓id, 房间总数>
        Map<Long, Long> roomCountMap = roomList.stream()
                .collect(Collectors.groupingBy(RoomInfo::getApartmentId, Collectors.counting()));
        // 根据公寓id列表查询已签约房间列表
        LambdaQueryWrapper<LeaseAgreement> leaseAgreementLambdaQueryWrapper = new LambdaQueryWrapper<>();
        leaseAgreementLambdaQueryWrapper.in(LeaseAgreement::getApartmentId, idList);
        leaseAgreementLambdaQueryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED);
        leaseAgreementLambdaQueryWrapper.eq(LeaseAgreement::getStatus, LeaseStatus.WITHDRAWING);


        List<LeaseAgreement> leaseAgreementList = leaseAgreementService.list(leaseAgreementLambdaQueryWrapper);
        // 将已签约房间列表转换为Map<公寓id, 已签约房间数>
        Map<Long, Long> signedRoomCountMap = leaseAgreementList.stream().collect(Collectors.groupingBy(LeaseAgreement::getApartmentId, Collectors.counting()));

        records.forEach(item -> {
            // 根据公寓id从map中取出对应公寓的房间数
            Long roomCount = roomCountMap.getOrDefault(item.getId(), 0L);
            item.setTotalRoomCount(roomCount);
            // 根据公寓id从map中取出对应公寓的已签约房间数
            Long signedRoomCount = signedRoomCountMap.getOrDefault(item.getId(), 0L);
            // 计算空闲房间数
            Long freeRoomCount = roomCount - signedRoomCount;
            item.setFreeRoomCount(freeRoomCount);
        });
        return result;
    }
}




