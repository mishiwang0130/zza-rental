package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.AttrValue;
import com.wxy.zzarental.web.app.service.AttrValueService;
import com.wxy.zzarental.web.app.mapper.AttrValueMapper;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【attr_value(房间基本属性值表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue>
    implements AttrValueService{

}




