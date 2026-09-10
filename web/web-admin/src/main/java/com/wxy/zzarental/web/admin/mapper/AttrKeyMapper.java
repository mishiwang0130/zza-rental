package com.wxy.zzarental.web.admin.mapper;

import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeyRespVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.model.AttrKey
*/
public interface AttrKeyMapper extends BaseMapper<AttrKey> {

    List<AttrKeyRespVO> selectAttrInfo();
}




