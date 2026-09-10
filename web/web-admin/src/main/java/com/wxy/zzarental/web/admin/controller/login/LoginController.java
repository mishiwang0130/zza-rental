package com.wxy.zzarental.web.admin.controller.login;

import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.service.LoginService;
import com.wxy.zzarental.web.admin.vo.login.CaptchaRespVO;
import com.wxy.zzarental.web.admin.vo.login.LoginReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserInfoRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台管理系统登录管理")
@RestController
@RequestMapping("/admin")
public class LoginController {

    @Resource
    private LoginService loginService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaRespVO> getCaptcha() {
        return Result.ok(AdminApiAssembler.toResponse(loginService.getCaptcha()));
    }

    @Operation(summary = "登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody @Validated LoginReqVO reqVO) {
        return Result.ok(loginService.login(AdminApiAssembler.toCommand(reqVO)));
    }

    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("info")
    public Result<SystemUserInfoRespVO> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();

        return Result.ok(AdminApiAssembler.toResponse(loginService.getLoginUserInfoById(userId)));
    }
}
