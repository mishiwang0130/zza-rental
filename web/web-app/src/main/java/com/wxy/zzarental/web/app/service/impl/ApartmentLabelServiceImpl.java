package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.ApartmentLabel;
import com.wxy.zzarental.web.app.service.ApartmentLabelService;
import com.wxy.zzarental.web.app.mapper.ApartmentLabelMapper;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【apartment_label(公寓标签关联表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class ApartmentLabelServiceImpl extends ServiceImpl<ApartmentLabelMapper, ApartmentLabel>
    implements ApartmentLabelService{
}
