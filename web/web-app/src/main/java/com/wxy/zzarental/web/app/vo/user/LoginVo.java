package com.wxy.zzarental.web.app.vo.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "APP端登录实体")
public class LoginVo {

    @NotBlank(message = "手机号码不能为空")
    @Schema(description = "手机号码")
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;
}
