package com.wxy.zzarental.common.util;

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

    /**
     * 获取手机验证码
     *
     * @param phone 电话
     * @return {@code String }
     * @author wxy
     * @date 2026/09/02
     */
    public static String getPhoneCaptcha(String phone){
        return RedisKeyConstant.APP_LOGIN_KEY + phone;
    }

    /**
     * 获取房间
     *
     * @param roomId 房间号
     * @return {@code String }
     * @author wxy
     * @date 2026/09/05
     */
    public static String getRoomKey(Long roomId) {
        return RedisKeyConstant.ROOM_KEY + roomId;
    }

    public static String getRoomPageKey(Long current,Long size) {
        //等一下，我需要思考之
        return RedisKeyConstant.ROOM_PAGE_KEY + current + ":" + size;
    }

    /**
     * 获取公寓key
     *
     * @param apartmentId 公寓id
     * @return {@code String }
     * @author wxy
     * @date 2026/09/05
     */
    public static String getApartmentKey(Long apartmentId) {
        return RedisKeyConstant.APARTMENT_KEY + apartmentId;
    }
}
