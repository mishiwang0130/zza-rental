package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wxy.zzarental.model.entity.SystemPost;
import com.baomidou.mybatisplus.spring.service.IService;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostItemVo;

/**
* @author liubo
* @description 针对表【system_post(岗位信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface SystemPostService extends IService<SystemPost> {

    IPage<SystemPostItemVo> page1(IPage<SystemPost> systemPostPage, String postName);
}
