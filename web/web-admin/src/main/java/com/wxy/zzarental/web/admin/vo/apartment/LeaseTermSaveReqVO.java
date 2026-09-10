package com.wxy.zzarental.web.admin.vo.apartment;

import lombok.Data;

@Data
public class LeaseTermSaveReqVO {

    private Long id;

    private Integer monthCount;

    private String unit;
}
