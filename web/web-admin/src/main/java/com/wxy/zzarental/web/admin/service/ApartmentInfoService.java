package com.wxy.zzarental.web.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentDetailRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentSaveReqVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author liubo
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Service
* @createDate 2023-07-24 15:48:00
*/
public interface ApartmentInfoService extends IService<ApartmentInfo> {

    void saveOrUpdateApart(ApartmentSaveReqVO apartmentSubmitVo);

    IPage<ApartmentItemRespVO> pageItem(Page<ApartmentItemRespVO> page, ApartmentPageReqVO queryVo);

    ApartmentDetailRespVO getDetailById(Long id);

    void removeApartmentById(Long id);
}
