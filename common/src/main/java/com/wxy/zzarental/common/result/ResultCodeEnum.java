package com.wxy.zzarental.common.result;

import lombok.Getter;
import org.apache.el.parser.Token;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    PARAM_ERROR(202, "参数不正确"),
    SERVICE_ERROR(203, "服务异常"),
    DATA_ERROR(204, "数据异常"),
    ILLEGAL_REQUEST(205, "非法请求"),
    REPEAT_SUBMIT(206, "重复提交"),
    DELETE_ERROR(207, "请先删除子集"),
    APARTMENTID_ERROR(208,"公寓id不存在"),
    VIEW_APPOINTMENT_NOT_EXIST(209,"该预约不存在"),
    VIEW_APPOINTMENT_ERROR(210,"该预约不属于当前用户"),
    LEASEAGREEMENT_NOT_EXIST(211,"租约不存在"),


    ADMIN_ACCOUNT_EXIST_ERROR(301, "账号已存在"),
    ADMIN_CAPTCHA_CODE_ERROR(302, "验证码错误"),
    ADMIN_CAPTCHA_CODE_EXPIRED(303, "验证码已过期"),
    ADMIN_CAPTCHA_CODE_NOT_FOUND(304, "未输入验证码"),


    ADMIN_LOGIN_AUTH(305, "未登陆"),
    ADMIN_ACCOUNT_NOT_EXIST_ERROR(306, "账号不存在"),
    ADMIN_ACCOUNT_ERROR(307, "用户名或密码错误"),
    ADMIN_ACCOUNT_DISABLED_ERROR(308, "该用户已被禁用"),
    ADMIN_ACCESS_FORBIDDEN(309, "无访问权限"),

    APP_LOGIN_AUTH(501, "未登陆"),
    APP_LOGIN_PHONE_EMPTY(502, "手机号码为空"),
    APP_LOGIN_CODE_EMPTY(503, "验证码为空"),
    APP_SEND_SMS_TOO_OFTEN(504, "验证法发送过于频繁"),
    APP_LOGIN_CODE_EXPIRED(505, "验证码已过期"),
    APP_LOGIN_CODE_ERROR(506, "验证码错误"),
    APP_ACCOUNT_DISABLED_ERROR(507, "该用户已被禁用"),
    APP_SEND_SMS_ERROR(508, "验证码发送失败"),



    TOKEN_EXPIRED(601, "token过期"),
    TOKEN_INVALID(602, "token非法"),
    TOKEN_NOT_EXIST(603,"token不存在"),

    USER_NOT_EXIST(701, "用户不存在"),
    PHONE_ERROR(702, "手机号不正确"),
    ROOM_ID_IS_NULL(703, "房间id不能为空"),
    ROOM_ID_ERROR(704,"房间id不存在" );


    private final Integer code;

    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
