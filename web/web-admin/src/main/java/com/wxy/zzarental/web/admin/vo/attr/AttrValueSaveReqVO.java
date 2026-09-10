package com.wxy.zzarental.web.admin.vo.attr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "属性值保存参数")
public class AttrValueSaveReqVO {

    @Schema(description = "属性值ID，更新时传入")
    private Long id;

    private String name;

    private Long attrKeyId;
}
