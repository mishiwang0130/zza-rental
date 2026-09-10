package com.wxy.zzarental.web.admin.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementPageReqVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementRespVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
* @author liubo
* @description 针对表【lease_agreement(租约信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.model.LeaseAgreement
*/
public interface LeaseAgreementMapper extends BaseMapper<LeaseAgreement> {

    IPage<AgreementRespVO> page(Page<LeaseAgreement> page, AgreementPageReqVO queryVo) ;
}