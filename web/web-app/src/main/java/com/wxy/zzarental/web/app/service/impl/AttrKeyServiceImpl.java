package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.web.app.service.AttrKeyService;
import com.wxy.zzarental.web.app.mapper.AttrKeyMapper;
import org.springframework.stereotype.Service;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class AttrKeyServiceImpl extends ServiceImpl<AttrKeyMapper, AttrKey>
    implements AttrKeyService{

}




