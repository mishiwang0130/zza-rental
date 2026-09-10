package com.wxy.zzarental.web.admin.service;

import com.wxy.zzarental.web.admin.vo.login.CaptchaRespVO;
import com.wxy.zzarental.web.admin.vo.login.LoginReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserInfoRespVO;

public interface LoginService {

    CaptchaRespVO getCaptcha();

    String login(LoginReqVO loginVo);

    SystemUserInfoRespVO getLoginUserInfoById(Long userId);
}
