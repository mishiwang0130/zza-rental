package com.wxy.zzarental.web.admin.service.command;

import lombok.Data;

@Data
public class LoginCommand {

    private String username;

    private String password;

    private String captchaKey;

    private String captchaCode;
}
