package com.wxy.zzarental.web.admin.service.query;

import lombok.Data;

@Data
public class AppointmentQuery {

    private Long provinceId;

    private Long cityId;

    private Long districtId;

    private Long apartmentId;

    private String name;

    private String phone;

}
