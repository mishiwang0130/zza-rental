package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.web.app.entity.*;
import com.wxy.zzarental.web.app.enums.ItemType;
import com.wxy.zzarental.web.app.enums.LeaseStatus;
import com.wxy.zzarental.web.app.mapper.*;
import com.wxy.zzarental.web.app.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.dto.AgreementDetailDTO;
import com.wxy.zzarental.web.app.service.dto.AgreementItemDTO;
import com.wxy.zzarental.web.app.service.dto.ApartmentDetailDTO;
import com.wxy.zzarental.web.app.service.dto.GraphDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.coyote.http11.Constants.a;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Resource
    private LeaseAgreementMapper leaseAgreementMapper;
    @Resource
    private GraphInfoService graphInfoService;
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private ApartmentInfoService apartmentInfoService;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private GraphInfoMapper graphInfoMapper;
    @Resource
    private  PaymentTypeMapper paymentTypeMapper;
    @Resource
    private LeaseTermMapper leaseTermMapper;



    @Override
    public List<AgreementItemDTO> listItemByPhone(String phone) {
        LambdaQueryWrapper<LeaseAgreement> leaseAgreementLambdaQueryWrapper = new LambdaQueryWrapper<>();
        leaseAgreementLambdaQueryWrapper.eq(LeaseAgreement::getPhone,phone);
        List<LeaseAgreement> leaseAgreements = leaseAgreementMapper.selectList(leaseAgreementLambdaQueryWrapper);
        if(CollUtil.isEmpty(leaseAgreements)){
            return Collections.emptyList();
        }
        //得到房间id列表
        List<Long> roomIds = leaseAgreements.stream().map(LeaseAgreement::getRoomId).toList();
        //房间图片列表
        Map<Long, List<GraphInfo>> graphMap = graphInfoService.mapByItemIds(2, roomIds);
        //房间信息
        List<RoomInfo> roomInfoList = roomInfoMapper.selectBatchIds(roomIds);
        Map<Long, RoomInfo> roomInfoMap = roomInfoList.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity(), (key1, key2) -> key1));
        //公寓信息

        return leaseAgreements.stream().map(
                leaseAgreement -> {
                    Long roomId = leaseAgreement.getRoomId();
                    AgreementItemDTO agreementItemVo = new AgreementItemDTO();
                    BeanUtil.copyProperties(leaseAgreement, agreementItemVo);
                    agreementItemVo.setLeaseStatus(leaseAgreement.getStatus());

                    //房间名称
                    RoomInfo roomInfo = roomInfoMap.get(roomId);
                    if (roomInfo != null) {
                        agreementItemVo.setRoomNumber(roomInfo.getRoomNumber());
                    }
                    List<GraphInfo> graphInfos = graphMap.get(roomId);
                    List<GraphDTO> graphVos = graphInfos.stream().map(
                            graphInfo -> {
                                return new GraphDTO(graphInfo.getName(), graphInfo.getUrl());
                            }
                    ).toList();
                    agreementItemVo.setRoomGraphVoList(graphVos);
                    //公寓
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
                    if (apartmentInfo != null) {
                        agreementItemVo.setApartmentName(apartmentInfo.getName());
                    }
                    return agreementItemVo;
                }
        ).toList();
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateLeaseAgreement(LeaseAgreement leaseAgreement) {
        //校验phone是否填本人的
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null){
            throw new ZZAException(ResultCodeEnum.USER_NOT_EXIST);
        }
        if (!userInfo.getPhone().equals(leaseAgreement.getPhone()) ){
            throw new ZZAException(ResultCodeEnum.PHONE_ERROR);
        }

        // 校验公寓id
        Long apartmentId = leaseAgreement.getApartmentId();
        if(apartmentId == null){
            throw  new ZZAException(ResultCodeEnum.APARTMENTID_ERROR);
        }
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(apartmentId);
        if (Objects.isNull(apartmentInfo)){
            throw  new ZZAException(ResultCodeEnum.APARTMENTID_ERROR);
        }

        // 校验房间id
        Long roomId = leaseAgreement.getRoomId();
        if (roomId == null){
            throw new ZZAException(ResultCodeEnum.ROOM_ID_IS_NULL);
        }
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (Objects.isNull(roomInfo)){
            throw new ZZAException(ResultCodeEnum.ROOM_ID_ERROR);
        }


        Long leaseAgreementId = leaseAgreement.getId();
        if (leaseAgreementId == null){
            leaseAgreementMapper.insert(leaseAgreement);
            return;
        }
        leaseAgreementMapper.updateById(leaseAgreement);

    }

    @Override
    public AgreementDetailDTO getDetailById(Long id) {
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null){
            throw  new ZZAException(ResultCodeEnum.LEASEAGREEMENT_NOT_EXIST);
        }
        String phone= LoginUserHolder.getLoginUser().getUserName();
        if (!leaseAgreement.getPhone().equals(phone)){
            throw new ZZAException(ResultCodeEnum.LEASE_AGREEMENT_ERROR);
        }
        AgreementDetailDTO agreementDetailVo = new AgreementDetailDTO();
        BeanUtil.copyProperties(leaseAgreement,agreementDetailVo);
        //公寓id
        Long apartmentId = leaseAgreement.getApartmentId();
        //公寓名称
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(apartmentId);
        if (apartmentInfo != null) {
            agreementDetailVo.setApartmentName(apartmentInfo.getName());
        }
        //公寓图片列表
        List<GraphDTO> apartmentGraphVoList = graphInfoMapper.selectListByIdAndType(apartmentId, ItemType.APARTMENT);
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
        //房间id
        Long roomId = leaseAgreement.getRoomId();
        //房间号
        agreementDetailVo.setRoomNumber(roomInfoMapper.selectById(roomId).getRoomNumber());
        //房间图片
        List<GraphDTO> roomGraphVoList = graphInfoMapper.selectListByIdAndType(roomId, ItemType.ROOM);
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
        //支付id
        Long paymentTypeId = leaseAgreement.getPaymentTypeId();
        //支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(paymentTypeId);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        //租期id
        Long leaseTermId = leaseAgreement.getLeaseTermId();
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseTermId);
        if (leaseTerm == null){
            throw new ZZAException(ResultCodeEnum.LEASE_ID_ERROR);
        }
        //租期月数
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        //租期单位
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());


        return agreementDetailVo;
    }
}




