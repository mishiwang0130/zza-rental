package com.wxy.zzarental.web.app.service;

public interface SmsService {
    /**
     * 发送验证码
     *
     * @param phone        电话
     * @param code         验证码
     * @param expireMinute 过期分钟
     * @author wxy
     * @date 2026/09/02
     */
    void sendCode(String phone,String code, String expireMinute);
}
