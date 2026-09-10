package com.wxy.zzarental.web.admin.service;

import com.wxy.zzarental.web.admin.service.command.LoginCommand;
import com.wxy.zzarental.web.admin.service.dto.CaptchaDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemUserInfoDTO;

public interface LoginService {

    CaptchaDTO getCaptcha();

    String login(LoginCommand loginVo);

    SystemUserInfoDTO getLoginUserInfoById(Long userId);
}
