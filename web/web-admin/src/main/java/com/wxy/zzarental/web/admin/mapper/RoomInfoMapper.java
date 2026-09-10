package com.wxy.zzarental.web.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.RoomInfo;
import com.wxy.zzarental.web.admin.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.admin.service.query.RoomQuery;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.model.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    IPage<RoomItemDTO> pageItem(Page<RoomItemDTO> page, RoomQuery queryVo);
}
