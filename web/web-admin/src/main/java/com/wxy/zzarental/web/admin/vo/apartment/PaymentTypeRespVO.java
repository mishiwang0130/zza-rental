package com.wxy.zzarental.web.admin.vo.apartment;

import lombok.Data;

@Data
public class PaymentTypeRespVO {

    private Long id;
    private String name;
    private String payMonthCount;
    private String additionalInfo;
}
