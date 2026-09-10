package com.wxy.zzarental.web.app.vo.common;

import com.wxy.zzarental.model.enums.ItemType;
import lombok.Data;

@Data
public class FacilityRespVO {

    private Long id;

    private ItemType type;
    private String name;
    private String icon;
}
