package com.wxy.zzarental.web.admin.vo.fee;

import lombok.Data;

@Data
public class FeeValueSaveReqVO {

    private Long id;

    private String name;

    private String unit;

    private Long feeKeyId;
}
