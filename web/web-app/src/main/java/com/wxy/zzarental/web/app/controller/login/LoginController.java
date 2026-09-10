package com.wxy.zzarental.web.app.controller.login;


import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.web.app.service.LoginService;
import com.wxy.zzarental.web.app.controller.assembler.AppApiAssembler;
import com.wxy.zzarental.web.app.vo.user.LoginReqVO;
import com.wxy.zzarental.web.app.vo.user.UserInfoRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
public class LoginController {
    @Resource
    private LoginService loginService;

    @GetMapping("login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        loginService.sendSmsCode(phone);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginReqVO loginVo) {
        return Result.ok(loginService.login(AppApiAssembler.toCommand(loginVo)));
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoRespVO> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();

        return Result.ok(AppApiAssembler.toResponse(loginService.getLoginUserById(userId)));
    }
}

