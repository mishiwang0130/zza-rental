package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.web.admin.service.command.ApartmentSaveCommand;
import com.wxy.zzarental.web.admin.service.dto.ApartmentDetailDTO;
import com.wxy.zzarental.web.admin.service.dto.ApartmentItemDTO;
import com.wxy.zzarental.web.admin.service.query.ApartmentQuery;

/**
* @author liubo
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface ApartmentInfoService extends IService<ApartmentInfo> {

    void saveOrUpdateApart(ApartmentSaveCommand apartmentSubmitVo);

    IPage<ApartmentItemDTO> pageItem(Page<ApartmentItemDTO> page, ApartmentQuery queryVo);

    ApartmentDetailDTO getDetailById(Long id);

    void removeApartmentById(Long id);
}
