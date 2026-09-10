package com.wxy.zzarental.web.admin.service.query;

import lombok.Data;

@Data
public class AgreementQuery {

    private Long provinceId;

    private Long cityId;

    private Long districtId;

    private Long apartmentId;

    private String roomNumber;

    private String name;

    private String phone;

}
