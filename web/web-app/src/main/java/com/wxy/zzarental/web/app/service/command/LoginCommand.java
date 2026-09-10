package com.wxy.zzarental.web.app.service.command;

import lombok.Data;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class LoginCommand {
    private String phone;
    private String code;
}
