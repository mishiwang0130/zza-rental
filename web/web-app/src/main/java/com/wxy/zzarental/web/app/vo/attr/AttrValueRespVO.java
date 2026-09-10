package com.wxy.zzarental.web.app.vo.attr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "属性值")
@Data
public class AttrValueRespVO {

    private Long id;

    @Schema(description = "属性值")
    private String name;

    @Schema(description = "属性名称ID")
    private Long attrKeyId;

    @Schema(description = "对应的属性key_name")
    private String attrKeyName;
}
