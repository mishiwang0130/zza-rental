package com.wxy.zzarental.web.app.mapper;

import com.wxy.zzarental.model.entity.GraphInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.web.app.service.dto.GraphDTO;

import java.util.List;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.wxy.zzarental.model.entity.GraphInfo
*/
public interface GraphInfoMapper extends BaseMapper<GraphInfo> {

    List<GraphDTO> selectListByIdAndType(Long id, ItemType itemType);
}




