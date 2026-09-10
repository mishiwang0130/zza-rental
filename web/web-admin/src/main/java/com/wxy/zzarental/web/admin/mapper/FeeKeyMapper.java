package com.wxy.zzarental.web.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxy.zzarental.model.entity.FeeKey;
import com.wxy.zzarental.web.admin.service.dto.FeeKeyDTO;
import java.util.List;

/**
* @author liubo
* @description 针对表【fee_key(杂项费用名称表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.model.FeeKey
*/
public interface FeeKeyMapper extends BaseMapper<FeeKey> {

    List<FeeKeyDTO> selectList();
}
