package com.wxy.zzarental.web.app.service.dto;

import lombok.Data;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class FeeValueDTO {

    private Long id;
    private String name;
    private String unit;
    private Long feeKeyId;
    private String feeKeyName;
}
