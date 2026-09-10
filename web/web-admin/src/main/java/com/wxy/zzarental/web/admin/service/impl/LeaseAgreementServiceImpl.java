package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.*;
import com.wxy.zzarental.web.admin.mapper.*;
import com.wxy.zzarental.web.admin.service.LeaseAgreementService;
import com.wxy.zzarental.web.admin.service.dto.AgreementDTO;
import com.wxy.zzarental.web.admin.service.query.AgreementQuery;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Resource
    private LeaseAgreementMapper leaseAgreementMapper;
    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private RoomInfoMapper roomInfoMapper;
    @Resource
    private PaymentTypeMapper paymentTypeMapper;
    @Resource
    private LeaseTermMapper leaseTermMapper;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public IPage<AgreementDTO> selectPage(Page<LeaseAgreement> page, AgreementQuery queryVo) {
        // 查询租约信息分页，得到一个IPage<LeaseAgreement>对象，对象中包含了租约信息列表，如查询十条记录
        IPage<AgreementDTO> agreementIPage = leaseAgreementMapper.page(page, queryVo);
        List<AgreementDTO> records = agreementIPage.getRecords();
        if(CollUtil.isEmpty(records)){
            return agreementIPage;
        }
        Set<Long> apartmentIdSet = records.stream()
                .map(AgreementDTO::getApartmentId).collect(Collectors.toSet());
        Set<Long> roomIdSet = records.stream()
                .map(AgreementDTO::getRoomId).collect(Collectors.toSet());
        List<Long> paymentTypeIdList = records.stream()
                .map(AgreementDTO::getPaymentTypeId).distinct().toList();
        List<Long> leaseTermIdList = records.stream().map(AgreementDTO::getLeaseTermId).distinct().toList();

        // 通过租约列表，查询出签约公寓信息、签约房间信息、支付方式、租期等信息，得到四个列表，每个列表也是十条记录
        List<ApartmentInfo> apartmentInfoList = apartmentInfoMapper.selectBatchIds(apartmentIdSet);
        List<RoomInfo> roomInfoList = roomInfoMapper.selectBatchIds(roomIdSet);
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectBatchIds(paymentTypeIdList);
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectBatchIds(leaseTermIdList);

        // 将这四个列表转换为四个map，map的key就是租约信息中与这些列表对应的那个字段，如：id，value就是对应的实体类对象
        Map<Long, ApartmentInfo> apartmentInfoMap = apartmentInfoList.stream().collect(Collectors.toMap(ApartmentInfo::getId, Function.identity()));
        Map<Long, RoomInfo> roomInfoMap = roomInfoList.stream().collect(Collectors.toMap(RoomInfo::getId, Function.identity()));
        Map<Long, PaymentType> paymentTypeMap = paymentTypeList.stream().collect(Collectors.toMap(PaymentType::getId, Function.identity()));
        Map<Long, LeaseTerm> leaseTermMap = leaseTermList.stream().collect(Collectors.toMap(LeaseTerm::getId, Function.identity()));

        // 转换为AgreementDTO列表
        // 给租约列表的每一个AgreementDTO添加签约公寓信息、签约房间信息、支付方式、租期等信息
        records.forEach(agreementVo -> {
            ApartmentInfo apartmentInfo = apartmentInfoMap.get(agreementVo.getApartmentId());
            agreementVo.setApartmentInfo(apartmentInfo);
            RoomInfo roomInfo = roomInfoMap.get(agreementVo.getRoomId());
            agreementVo.setRoomInfo(roomInfo);
            PaymentType paymentType = paymentTypeMap.get(agreementVo.getPaymentTypeId());
            agreementVo.setPaymentType(paymentType);
            LeaseTerm leaseTerm = leaseTermMap.get(agreementVo.getLeaseTermId());
            agreementVo.setLeaseTerm(leaseTerm);

        });

        agreementIPage.setRecords(records);
        // 返回租约列表
        return agreementIPage;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AgreementDTO getLeaseInfoById(Long id) {
        AgreementDTO agreementVo = new AgreementDTO();
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        // 这里没错
        BeanUtils.copyProperties(leaseAgreement,agreementVo);
        //公寓
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());
        agreementVo.setApartmentInfo(apartmentInfo);
        //房间
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());
        agreementVo.setRoomInfo(roomInfo);
        //支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());
        agreementVo.setPaymentType(paymentType);
        //租期
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());
        agreementVo.setLeaseTerm(leaseTerm);

        return agreementVo;
    }
}
