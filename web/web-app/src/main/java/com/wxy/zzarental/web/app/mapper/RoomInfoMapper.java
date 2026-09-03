package com.wxy.zzarental.web.app.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxy.zzarental.web.app.vo.room.RoomItemVo;
import com.wxy.zzarental.web.app.vo.room.RoomQueryVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.wxy.zzarental.model.entity.RoomInfo
*/
public interface RoomInfoMapper extends BaseMapper<RoomInfo> {

    /**
     * 选择最低租金
     *
     * @param apartmentId 公寓id
     * @return {@code BigDecimal }
     * @author wxy
     * @date 2026/09/03
     */
    BigDecimal selectMinRent(@Param("apartmentId") Long apartmentId);

    IPage<RoomItemVo> pageItem(@Param("page") Page<RoomItemVo> page,
                               @Param("queryVo") RoomQueryVo queryVo,
                               @Param("payRoomIds") List<Long> payRoomIds);
}