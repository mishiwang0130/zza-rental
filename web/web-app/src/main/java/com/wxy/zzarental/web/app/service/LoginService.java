package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.web.app.service.command.LoginCommand;
import com.wxy.zzarental.web.app.service.dto.UserInfoDTO;

public interface LoginService {

    /**
     * 登录
     *
     * @param loginVo 登录vo
     * @return {@code String }
     * @author wxy
     * @date 2026/09/02
     */
    String login(LoginCommand loginVo);
    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return {@code UserInfoDTO }
     * @author wxy
     * @date 2026/09/02
     */
    UserInfoDTO getLoginUserById(Long userId);

    /**
     * 获取验证码
     *
     * @param phone 电话
     * @author wxy
     * @date 2026/09/02
     */
    void sendSmsCode(String phone);
}
