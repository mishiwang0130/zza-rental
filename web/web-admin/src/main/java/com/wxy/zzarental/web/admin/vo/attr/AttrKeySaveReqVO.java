package com.wxy.zzarental.web.admin.vo.attr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "属性名称保存参数")
public class AttrKeySaveReqVO {

    @Schema(description = "属性名称ID，更新时传入")
    private Long id;

    @Schema(description = "属性名称")
    private String name;
}
