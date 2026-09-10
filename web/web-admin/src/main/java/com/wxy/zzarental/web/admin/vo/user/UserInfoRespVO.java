package com.wxy.zzarental.web.admin.vo.user;

import com.wxy.zzarental.web.admin.enums.BaseStatus;
import lombok.Data;

@Data
public class UserInfoRespVO {
    private Long id;
    private String phone;
    private String avatarUrl;
    private String nickname;
    private BaseStatus status;

    /** 保留原账号列表的 password: null 字段，不向响应复制密码。 */
    public String getPassword() {
        return null;
    }
}
