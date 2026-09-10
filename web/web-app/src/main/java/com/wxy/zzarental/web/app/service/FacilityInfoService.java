package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.FacilityInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface FacilityInfoService extends IService<FacilityInfo> {

    /**
     * 按房间id列出
     *
     * @param roomId 房间号
     * @return {@code List<FacilityInfo> }
     * @author wxy
     * @date 2026/09/03
     */
    List<FacilityInfo> listByRoomId(Long roomId);
}
