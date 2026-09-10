package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.web.admin.service.dto.AgreementDTO;
import com.wxy.zzarental.web.admin.service.query.AgreementQuery;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {

    IPage<AgreementDTO> selectPage(Page<LeaseAgreement> page, AgreementQuery queryVo);

    AgreementDTO getLeaseInfoById(Long id);
}
