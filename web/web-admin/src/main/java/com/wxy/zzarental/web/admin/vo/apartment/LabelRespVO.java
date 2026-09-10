package com.wxy.zzarental.web.admin.vo.apartment;

import com.wxy.zzarental.model.enums.ItemType;
import lombok.Data;

@Data
public class LabelRespVO {

    private Long id;
    private ItemType type;
    private String name;
}
