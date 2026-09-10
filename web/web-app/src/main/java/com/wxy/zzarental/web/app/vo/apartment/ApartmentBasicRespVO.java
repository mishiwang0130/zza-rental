package com.wxy.zzarental.web.app.vo.apartment;

import com.wxy.zzarental.web.app.enums.ReleaseStatus;
import lombok.Data;

@Data
public class ApartmentBasicRespVO {

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
}
