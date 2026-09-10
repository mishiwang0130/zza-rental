package com.wxy.zzarental.web.admin.service;

import com.wxy.zzarental.web.admin.entity.CityInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【city_info】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface CityInfoService extends IService<CityInfo> {

    List<CityInfo> listCityInfoByProvinceId(Long id);
}
