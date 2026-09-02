package com.wxy.zzarental.common.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String userName;
}
