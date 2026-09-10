package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserPageReqVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【system_user(员工信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface SystemUserService extends IService<SystemUser> {

    IPage<SystemUserItemRespVO> pageUser(Page<SystemUser> systemUserPage, SystemUserPageReqVO queryVo);

    SystemUserItemRespVO getSystemUserById(Long id);
}
