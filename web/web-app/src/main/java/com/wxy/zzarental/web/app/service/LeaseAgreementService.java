package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.app.vo.agreement.AgreementDetailRespVO;
import com.wxy.zzarental.web.app.vo.agreement.AgreementItemRespVO;

import java.util.List;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface LeaseAgreementService extends IService<LeaseAgreement> {
    List<AgreementItemRespVO> listItemByPhone(String phone);

    void saveOrUpdateLeaseAgreement(LeaseAgreement leaseAgreement);

    /**
     * 按id获取租约详细信息
     *
     * @param id ID
     * @return {@code AgreementDetailRespVO }
     * @author wxy
     * @date 2026/09/05
     */
    AgreementDetailRespVO getDetailById(Long id);
}
