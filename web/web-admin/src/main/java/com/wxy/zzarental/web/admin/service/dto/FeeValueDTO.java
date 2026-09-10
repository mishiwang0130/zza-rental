package com.wxy.zzarental.web.admin.service.dto;

import lombok.Data;

@Data
public class FeeValueDTO {

    private Long id;

    private String name;

    private String unit;

    private Long feeKeyId;

    private String feeKeyName;
}
