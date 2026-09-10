package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementPageReqVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {

    IPage<AgreementRespVO> selectPage(Page<LeaseAgreement> page, AgreementPageReqVO queryVo);

    AgreementRespVO getLeaseInfoById(Long id);
}
