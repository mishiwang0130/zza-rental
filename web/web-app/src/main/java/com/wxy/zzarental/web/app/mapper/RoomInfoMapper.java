package com.wxy.zzarental.web.app.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.web.app.entity.RoomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxy.zzarental.web.app.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.app.service.query.RoomQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
* @author liubo
* @description 针对表【room_info(房间信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.wxy.zzarental.web.app.entity.RoomInfo
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

    IPage<RoomItemDTO> pageItem(@Param("page") Page<RoomItemDTO> page,
                               @Param("queryVo") RoomQuery queryVo,
                               @Param("payRoomIds") List<Long> payRoomIds);

    IPage<RoomItemDTO> pageItemByApartmentId(Page<RoomItemDTO> page, Long id);
}
