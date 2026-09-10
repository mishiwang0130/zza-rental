package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.model.enums.ReleaseStatus;
import lombok.Data;

@Data
public class ApartmentItemDTO {

    private Long id;

    private String name;
    private String introduction;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    private Long provinceId;
    private String provinceName;
    private String addressDetail;
    private String latitude;
    private String longitude;
    private String phone;
    private ReleaseStatus isRelease;

    private Long totalRoomCount;

    private Long freeRoomCount;

}
