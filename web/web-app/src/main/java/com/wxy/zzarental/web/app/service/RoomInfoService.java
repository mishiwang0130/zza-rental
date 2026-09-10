package com.wxy.zzarental.web.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.RoomInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.app.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomPageReqVO;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface RoomInfoService extends IService<RoomInfo> {
    /**
     * 按降序或升序排序的分页
     *
     * @param page 页
     * @return {@code IPage<RoomItemRespVO> }
     * @author wxy
     * @date 2026/09/03
     */
    IPage<RoomItemRespVO> pageItem(Page<RoomItemRespVO> page, RoomPageReqVO queryVo);

    RoomDetailRespVO getDetailById(Long id);

    IPage<RoomItemRespVO> pageItemByApartmentId(Page<RoomItemRespVO> page, Long id);
}
