package com.wxy.zzarental.web.admin.vo.fee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
public class FeeKeyRespVO {

    private Long id;

    @Schema(description = "杂费名称")
    private String name;

    @Schema(description = "杂费value列表")
    @JsonIgnoreProperties("feeKeyName")
    private List<FeeValueRespVO> feeValueList;
}
