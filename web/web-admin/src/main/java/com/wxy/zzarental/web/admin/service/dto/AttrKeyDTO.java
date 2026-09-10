package com.wxy.zzarental.web.admin.service.dto;

import java.util.List;
import lombok.Data;

@Data
public class AttrKeyDTO {

    private Long id;

    private String name;

    private List<AttrValueDTO> attrValueList;
}
