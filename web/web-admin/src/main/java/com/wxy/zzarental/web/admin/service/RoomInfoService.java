package com.wxy.zzarental.web.admin.service;

import com.wxy.zzarental.model.entity.RoomInfo;
import com.wxy.zzarental.web.admin.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomPageReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomSaveReqVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface RoomInfoService extends IService<RoomInfo> {

    void saveOrUpdateRoom(RoomSaveReqVO roomSubmitVo);

    IPage<RoomItemRespVO> pageItem(long current, long size, RoomPageReqVO queryVo);

    RoomDetailRespVO getDetailById(Long id);

    void removeRoomById(Long id);
}
