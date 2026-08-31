package com.wxy.zzarental.common.util;

import cn.hutool.core.lang.UUID;
import com.wxy.zzarental.common.constant.RedisKeyConstant;


/**
 * @author wxy
 * @description Redis key工具类
 * @date 2026/08/31
 */
public class RedisKeyUtil {

    /**
     * 获取验证码
     *
     * @param key key
     * @return {@code String }
     * @author wxy
     * @date 2026/08/31
     */
    public static String getCaptcha(String key) {
        return RedisKeyConstant.CAPTCHA_KEY + key;
    }
}
