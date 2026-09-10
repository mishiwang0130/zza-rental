package com.wxy.zzarental.web.app.vo.attr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class AttrKeyRespVO {

    private Long id;

    @Schema(description = "属性名称")
    private String name;

    @Schema(description = "属性value列表")
    @JsonIgnoreProperties("attrKeyName")
    private List<AttrValueRespVO> attrValueList;
}
