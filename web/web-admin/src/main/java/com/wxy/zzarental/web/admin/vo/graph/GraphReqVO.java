package com.wxy.zzarental.web.admin.vo.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图片请求参数")
public class GraphReqVO {

    @Schema(description = "图片名称")
    private String name;

    @Schema(description = "图片URL")
    private String url;
}
