package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.admin.entity.RoomInfo;
import com.wxy.zzarental.web.admin.service.command.RoomSaveCommand;
import com.wxy.zzarental.web.admin.service.dto.RoomDetailDTO;
import com.wxy.zzarental.web.admin.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.admin.service.query.RoomQuery;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface RoomInfoService extends IService<RoomInfo> {

    void saveOrUpdateRoom(RoomSaveCommand roomSubmitVo);

    IPage<RoomItemDTO> pageItem(long current, long size, RoomQuery queryVo);

    RoomDetailDTO getDetailById(Long id);

    void removeRoomById(Long id);
}
