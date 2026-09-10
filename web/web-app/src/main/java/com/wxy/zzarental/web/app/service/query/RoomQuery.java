package com.wxy.zzarental.web.app.service.query;

import lombok.Data;

import java.math.BigDecimal;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class RoomQuery {
    private Long provinceId;
    private Long cityId;
    private Long districtId;
    private BigDecimal minRent;
    private BigDecimal maxRent;
    private Long paymentTypeId;
    private String orderType;
}
