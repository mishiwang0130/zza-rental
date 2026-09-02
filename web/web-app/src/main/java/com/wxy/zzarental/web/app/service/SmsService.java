package com.wxy.zzarental.web.app.service;

public interface SmsService {
    void sendCode(String phone,String code);
}
