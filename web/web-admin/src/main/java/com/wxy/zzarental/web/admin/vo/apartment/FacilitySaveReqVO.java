package com.wxy.zzarental.web.admin.vo.apartment;

import com.wxy.zzarental.web.admin.enums.ItemType;
import lombok.Data;

@Data
public class FacilitySaveReqVO {

    private Long id;

    private ItemType type;

    private String name;

    private String icon;
}
