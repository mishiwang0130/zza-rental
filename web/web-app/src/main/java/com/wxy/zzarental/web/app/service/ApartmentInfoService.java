package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemVo;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service
 * @createDate 2023-07-26 11:12:39
 */
public interface ApartmentInfoService extends IService<ApartmentInfo> {
    /**
     * 按id获取信息
     *
     * @param apartmentId 公寓id
     * @return {@code ApartmentItemVo }
     * @author wxy
     * @date 2026/09/03
     */
    ApartmentItemVo getInfoById(Long apartmentId);
}
