package com.wxy.zzarental.common.constant;

/**
 * @author wxy
 * @description redis key常数
 * @date 2026/08/31
 */
public interface RedisKeyConstant {
    /**
     * 前缀键
     */
    String PREFIX_KEY = "zza:";
    /**
     * admin验证码key
     */
    String CAPTCHA_KEY = PREFIX_KEY + "admin:captcha:";

    /**
     * 应用登录密钥
     */
    String APP_LOGIN_KEY=PREFIX_KEY + "app:captcha";
}
