package com.wxy.zzarental.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.CityInfo;
import com.wxy.zzarental.web.admin.mapper.LabelInfoMapper;
import com.wxy.zzarental.web.admin.service.CityInfoService;
import com.wxy.zzarental.web.admin.mapper.CityInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【city_info】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class CityInfoServiceImpl extends ServiceImpl<CityInfoMapper, CityInfo>
    implements CityInfoService{
    @Resource
    private CityInfoMapper cityInfoMapper;

    @Override
    public List<CityInfo> listCityInfoByProvinceId(Long id) {
        LambdaQueryWrapper<CityInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CityInfo::getProvinceId, id);
        return list(queryWrapper);
    }
}




