package com.wxy.zzarental.web.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.web.admin.entity.ApartmentInfo;
import com.wxy.zzarental.web.admin.service.dto.ApartmentItemDTO;
import com.wxy.zzarental.web.admin.service.query.ApartmentQuery;

/**
* @author liubo
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.web.admin.entity.ApartmentInfo
*/
public interface ApartmentInfoMapper extends BaseMapper<ApartmentInfo> {
    IPage<ApartmentItemDTO> pageItem(Page<ApartmentItemDTO> page, ApartmentQuery queryVo);
}
