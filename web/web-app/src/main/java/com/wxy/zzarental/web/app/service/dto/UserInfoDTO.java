package com.wxy.zzarental.web.app.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
@AllArgsConstructor
public class UserInfoDTO {
    private String nickname;
    private String avatarUrl;
}
