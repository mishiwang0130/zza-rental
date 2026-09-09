package com.wxy.zzarental.web.admin.service;

import com.wxy.zzarental.model.entity.DistrictInfo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【district_info】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface DistrictInfoService extends IService<DistrictInfo> {

    List<DistrictInfo> listDistrictInfoByCityId(Long id);
}
