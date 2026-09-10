package com.wxy.zzarental.web.admin.vo.fee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "杂费值")
@Data
public class FeeValueRespVO {

    private Long id;

    @Schema(description = "杂费值")
    private String name;

    @Schema(description = "收费单位")
    private String unit;

    @Schema(description = "杂费名称ID")
    private Long feeKeyId;

    @Schema(description = "费用所对的fee_key名称")
    private String feeKeyName;
}
