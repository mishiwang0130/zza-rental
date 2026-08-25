package com.wxy.zzarental.web.admin.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.mapper.LeaseAgreementMapper;
import com.wxy.zzarental.web.admin.service.LeaseAgreementService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTasks {

    @Resource
    private LeaseAgreementMapper leaseAgreementMapper;
    @Scheduled(cron ="0 0 0 * * *")
    public  void  CheckLeaseStatus(){
        LambdaUpdateWrapper<LeaseAgreement> leaseAgreementLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        leaseAgreementLambdaUpdateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED,LeaseStatus.WITHDRAWING);
        leaseAgreementLambdaUpdateWrapper.le(LeaseAgreement::getLeaseEndDate,new Date());
        leaseAgreementLambdaUpdateWrapper.set(LeaseAgreement::getStatus,LeaseStatus.EXPIRED);
        leaseAgreementMapper.update(null,leaseAgreementLambdaUpdateWrapper);

    }
}
