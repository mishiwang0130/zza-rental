package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.mapper.*;
import com.wxy.zzarental.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentDetailRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.FacilityRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LabelRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentSaveReqVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeValueRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphReqVO;
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
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private  LabelInfoMapper labelInfoMapper;
    @Resource
    private FacilityInfoMapper facilityInfoMapper;
    @Resource
    private FeeValueMapper feeValueMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateApart(ApartmentSaveReqVO apartmentSubmitVo) {
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
        List<GraphReqVO> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(CollUtil.isNotEmpty(graphVoList)){
            List<GraphInfo> graphInfoList = graphVoList.stream().map(item -> {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(item.getName());
                graphInfo.setUrl(item.getUrl());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                return graphInfo;
            }).toList();
            graphInfoService.saveBatch(graphInfoList);
        }



        //新增设施
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        if(CollUtil.isNotEmpty(facilityInfoIds)) {
            List<ApartmentFacility> facilityList = facilityInfoIds.stream().map(item -> {
                return ApartmentFacility.builder().apartmentId(apartmentSubmitVo.getId()).facilityId(item).build();
            }).toList();
            apartmentFacilityService.saveBatch(facilityList);
        }
        //新增标签
        List<Long> labelInfoIds = apartmentSubmitVo.getLabelIds();
        if(CollUtil.isNotEmpty(labelInfoIds)){
            List<ApartmentLabel> labelList = labelInfoIds.stream().map(item -> {
                return ApartmentLabel.builder().apartmentId(apartmentSubmitVo.getId()).labelId(item).build();
            }).toList();
            apartmentLabelService.saveBatch(labelList);
        }

        //新增杂费值
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if(CollUtil.isNotEmpty(feeValueIds)) {
            List<ApartmentFeeValue> feeValueList = feeValueIds.stream()
                    .map(item -> ApartmentFeeValue.builder()
                            .apartmentId(apartmentSubmitVo.getId()).feeValueId(item).build())
                    .toList();
            apartmentFeeValueService.saveBatch(feeValueList);
        }
    }


    @Override
    public IPage<ApartmentItemRespVO> pageItem(Page<ApartmentItemRespVO> page, ApartmentPageReqVO queryVo) {
        IPage<ApartmentItemRespVO> result = apartmentInfoMapper.pageItem(page, queryVo);
        List<ApartmentItemRespVO> records = result.getRecords();
        // 获取公寓id
        List<Long> idList = records.stream().map(ApartmentItemRespVO::getId).toList();
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

    @Override
    public ApartmentDetailRespVO getDetailById(Long id) {
        //公寓基础信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //图片列表
//        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
//        graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
//        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id);
//        List<GraphInfo> graphInfoList = graphInfoService.list(graphInfoQueryWrapper);
//        // 转换为GraphRespVO列表
//        List<GraphRespVO> graphVoList = graphInfoList.stream().map(item -> {
//            GraphRespVO graphVo = new GraphRespVO();
//            graphVo.setName(item.getName());
//            graphVo.setUrl(item.getUrl());
//            return graphVo;
//        }).toList();
        List<GraphRespVO> graphVoList = graphInfoMapper.selectListByIdAndType(id, ItemType.APARTMENT);
        //标签列表
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);

        //配套列表
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);
        //杂费列表
        List<FeeValueRespVO> feeValueList = feeValueMapper.selectListByApartmentId(id);

        ApartmentDetailRespVO apartmentDetailVo = new ApartmentDetailRespVO();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(VOConverter.toList(labelInfoList, LabelRespVO.class));
        apartmentDetailVo.setFacilityInfoList(VOConverter.toList(facilityInfoList, FacilityRespVO.class));
        apartmentDetailVo.setFeeValueVoList(feeValueList);
        return apartmentDetailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeApartmentById(Long id) {
        removeById(id);
        LambdaQueryWrapper<RoomInfo> roomInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getApartmentId, id);
        if(roomInfoMapper.selectCount(roomInfoLambdaQueryWrapper)>0){
            throw new ZZAException(310,"公寓下有房间，是否确认删除");
        }


        LambdaQueryWrapper<GraphInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GraphInfo::getItemId, id);
        queryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoService.remove(queryWrapper);

        // 查询配套设施关联表，删除关联的设施记录
        LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(facilityQueryWrapper);

        //标签关联表，删除关联的标签记录
        LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(labelQueryWrapper);
        //杂费值关联表，删除关联的杂费值记录
        LambdaQueryWrapper<ApartmentFeeValue> feeQueryWrapper = new LambdaQueryWrapper<>();
        feeQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(feeQueryWrapper);
    }
}
