package com.wxy.zzarental.web.admin.service.dto;

import lombok.Data;

@Data
public class AttrValueDTO {

    private Long id;

    private String name;

    private Long attrKeyId;

    private String attrKeyName;
}
