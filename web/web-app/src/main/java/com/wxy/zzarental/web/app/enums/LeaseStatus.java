package com.wxy.zzarental.web.app.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum LeaseStatus implements BaseEnum {

    SIGNING(1, "签约待确认"),
    SIGNED(2, "已签约"),
    CANCELED(3, "已取消"),
    EXPIRED(4, "已到期"),
    WITHDRAWING(5, "退租待确认"),
    WITHDRAWN(6, "已退租"),
    RENEWING(7, "续约待确认");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    LeaseStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LeaseStatus isExistById(Integer leaseStatus) {
        return Arrays.stream(LeaseStatus.values())
                .filter(item -> item.getCode().equals(leaseStatus))
                .findFirst().orElseThrow(() -> new RuntimeException("不支持的枚举类型"));
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
