package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.mapper.*;
import com.wxy.zzarental.web.admin.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.FacilityRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LabelRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.admin.vo.graph.GraphReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomPageReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomSaveReqVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private GraphInfoService graphInfoService;
    @Resource
    private RoomAttrValueService roomAttrValueService;
    @Resource
    private RoomFacilityService roomFacilityService;
    @Resource
    private RoomLabelService roomLabelService;
    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;
    @Resource
    private RoomLeaseTermService roomLeaseTermService;
    @Resource
    private LeaseAgreementService leaseAgreementService;
    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private AttrValueMapper attrValueMapper;
    @Resource
    private AttrKeyMapper attrKeyMapper;
    @Resource
    private FacilityInfoMapper facilityInfoMapper;
    @Resource
    private LabelInfoMapper labelInfoMapper;
    @Resource
    private PaymentTypeMapper paymentTypeMapper;
    @Resource
    private LeaseTermMapper leaseTermMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateRoom(RoomSaveReqVO roomSubmitVo) {
        RoomInfo roomInfo = new RoomInfo();
        BeanUtils.copyProperties(roomSubmitVo, roomInfo);
        saveOrUpdate(roomInfo);
        if(roomSubmitVo.getId() != null){
//            roomInfoMapper.deleteById(roomSubmitVo.getId());
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
            LambdaQueryWrapper<RoomAttrValue> attrValueInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            attrValueInfoLambdaQueryWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrValueInfoLambdaQueryWrapper);

            LambdaQueryWrapper<RoomFacility> facilityInfoLambdaQueryWrapper = new LambdaQueryWrapper<RoomFacility>();
            facilityInfoLambdaQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(facilityInfoLambdaQueryWrapper);

            LambdaQueryWrapper<RoomLabel> labelInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            labelInfoLambdaQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(labelInfoLambdaQueryWrapper);

            LambdaQueryWrapper<RoomPaymentType> paymentTypeInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            paymentTypeInfoLambdaQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentTypeInfoLambdaQueryWrapper);

            LambdaQueryWrapper<RoomLeaseTerm> leaseTermInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            leaseTermInfoLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(leaseTermInfoLambdaQueryWrapper);
        }
        List<GraphReqVO> graphVoList = roomSubmitVo.getGraphVoList();
        if(CollUtil.isNotEmpty(graphVoList)){
            List<GraphInfo> graphInfoList = graphVoList.stream().map(item -> {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(item.getName());
                graphInfo.setUrl(item.getUrl());
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                return graphInfo;
            }).toList();
            graphInfoService.saveBatch(graphInfoList);
        }
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(CollUtil.isNotEmpty(attrValueIds)){
           List<RoomAttrValue> roomAttrValueList = attrValueIds.stream().map(item -> {
                return RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(item).build();
            }).toList();
            roomAttrValueService.saveBatch(roomAttrValueList);
        }
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (CollUtil.isNotEmpty(facilityInfoIds)) {
            ArrayList<RoomFacility> roomFacilityList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (CollUtil.isNotEmpty(labelInfoIds)) {
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (CollUtil.isNotEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (CollUtil.isNotEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }



    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public IPage<RoomItemRespVO> pageItem(long current, long size, RoomPageReqVO queryVo) {
        Page<RoomItemRespVO> page = new Page<>(current, size);
        IPage<RoomItemRespVO> result = roomInfoMapper.pageItem(page, queryVo);
        List<RoomItemRespVO> records = result.getRecords();

        List<Long> roomIds = records.stream().map(RoomItemRespVO::getId).toList();

        if (CollUtil.isNotEmpty(roomIds)) {
            LambdaQueryWrapper<LeaseAgreement> leaseAgreementQueryWrapper = new LambdaQueryWrapper<>();
            leaseAgreementQueryWrapper.in(LeaseAgreement::getRoomId, roomIds);
            leaseAgreementQueryWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
            List<LeaseAgreement> leaseAgreementList = leaseAgreementService.list(leaseAgreementQueryWrapper);

            Map<Long, LeaseAgreement> leaseAgreementMap = leaseAgreementList.stream()
                    .collect(Collectors.toMap(LeaseAgreement::getRoomId, a -> a, (a, b) -> a));

            List<Long> apartmentIds = records.stream().map(RoomItemRespVO::getApartmentId).distinct().toList();
            Map<Long, ApartmentInfo> apartmentInfoMap = apartmentInfoMapper.selectBatchIds(apartmentIds).stream()
                    .collect(Collectors.toMap(ApartmentInfo::getId, a -> a));

            records.forEach(item -> {
                LeaseAgreement leaseAgreement = leaseAgreementMap.get(item.getId());
                if (leaseAgreement != null) {
                    item.setLeaseEndDate(leaseAgreement.getLeaseEndDate());
                    item.setIsCheckIn(true);
                } else {
                    item.setIsCheckIn(false);
                }
                item.setApartmentInfo(VOConverter.to(apartmentInfoMap.get(item.getApartmentId()), ApartmentBasicRespVO.class));
            });
        }

        return result;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RoomDetailRespVO getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            return null;
        }
        RoomDetailRespVO roomDetailVo = new RoomDetailRespVO();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);
        // 设置公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomDetailVo.getApartmentId());
        roomDetailVo.setApartmentInfo(VOConverter.to(apartmentInfo, ApartmentBasicRespVO.class));
        // 设置图片列表
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = Wrappers.<GraphInfo>lambdaQuery()
                .eq(GraphInfo::getItemType, ItemType.ROOM)
                .eq(GraphInfo::getItemId, roomInfo.getId());
        List<GraphInfo> graphInfoList = graphInfoService.list(graphInfoLambdaQueryWrapper);
        List<GraphRespVO> graphVoList = graphInfoList.stream()
                .map(item -> GraphRespVO.builder().name(item.getName()).url(item.getUrl()).build())
                .collect(Collectors.toList());
        roomDetailVo.setGraphVoList(graphVoList);
        // 设置属性信息列表
        List<RoomAttrValue> roomAttrValueList = roomAttrValueService.list(new LambdaQueryWrapper<RoomAttrValue>()
                .eq(RoomAttrValue::getRoomId, roomInfo.getId()));
        List<AttrValue> attrValueList = attrValueMapper.selectBatchIds(roomAttrValueList.stream().map(RoomAttrValue::getAttrValueId).toList());
        List<AttrKey> attrKeyList = attrKeyMapper.selectBatchIds(attrValueList.stream().map(AttrValue::getAttrKeyId).toList());
        Map<Long, String> attrKeyMap = attrKeyList.stream().collect(Collectors.toMap(AttrKey::getId, AttrKey::getName, (a, b) -> a));
        roomDetailVo.setAttrValueVoList(attrValueList.stream()
                .map(item -> {
                    AttrValueRespVO attrValueVo = new AttrValueRespVO();
                    BeanUtils.copyProperties(item, attrValueVo);
                    attrValueVo.setAttrKeyName(attrKeyMap.get(item.getAttrKeyId()));
                    return attrValueVo;
                })
                .collect(Collectors.toList()));
        // 设置配套信息列表
        List<RoomFacility> roomFacilityList = roomFacilityService.list(new LambdaQueryWrapper<RoomFacility>()
                .eq(RoomFacility::getRoomId, roomInfo.getId()));
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectBatchIds(roomFacilityList.stream().map(RoomFacility::getFacilityId).toList());
        roomDetailVo.setFacilityInfoList(VOConverter.toList(facilityInfoList, FacilityRespVO.class));
        // 设置标签信息列表

        List<RoomLabel> roomLabelList = roomLabelService.list(new LambdaQueryWrapper<RoomLabel>()
                .eq(RoomLabel::getRoomId, roomInfo.getId()));
        List<LabelInfo> labelInfoList = labelInfoMapper.selectBatchIds(roomLabelList.stream().map(RoomLabel::getLabelId).toList());
        roomDetailVo.setLabelInfoList(VOConverter.toList(labelInfoList, LabelRespVO.class));

        // 设置支付方式列表
        List<RoomPaymentType> roomPaymentTypeList = roomPaymentTypeService.list(new LambdaQueryWrapper<RoomPaymentType>()
                .eq(RoomPaymentType::getRoomId, id));
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectBatchIds(roomPaymentTypeList.stream().map(RoomPaymentType::getPaymentTypeId).toList());
        roomDetailVo.setPaymentTypeList(VOConverter.toList(paymentTypeList, PaymentTypeRespVO.class));
        // 设置可选租期列表
        List<RoomLeaseTerm> roomLeaseTermList = roomLeaseTermService.list(new LambdaQueryWrapper<RoomLeaseTerm>()
                .eq(RoomLeaseTerm::getRoomId, roomInfo.getId()));
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectBatchIds(roomLeaseTermList.stream().map(RoomLeaseTerm::getLeaseTermId).toList());
        roomDetailVo.setLeaseTermList(VOConverter.toList(leaseTermList, LeaseTermRespVO.class));
        return roomDetailVo;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeRoomById(Long id) {
        roomInfoMapper.deleteById(id);
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphInfoLambdaQueryWrapper);
        LambdaQueryWrapper<RoomAttrValue> attrValueInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attrValueInfoLambdaQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrValueInfoLambdaQueryWrapper);

        LambdaQueryWrapper<RoomFacility> facilityInfoLambdaQueryWrapper = new LambdaQueryWrapper<RoomFacility>();
        facilityInfoLambdaQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityInfoLambdaQueryWrapper);

        LambdaQueryWrapper<RoomLabel> labelInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        labelInfoLambdaQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelInfoLambdaQueryWrapper);

        LambdaQueryWrapper<RoomPaymentType> paymentTypeInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        paymentTypeInfoLambdaQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentTypeInfoLambdaQueryWrapper);

        LambdaQueryWrapper<RoomLeaseTerm> leaseTermInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        leaseTermInfoLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(leaseTermInfoLambdaQueryWrapper);
    }
}
