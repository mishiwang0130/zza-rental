package com.wxy.zzarental.web.admin.vo.system.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.model.enums.SystemUserType;
import lombok.Data;

@Data
public class SystemUserSaveReqVO {

    private Long id;

    private String username;

    @JsonIgnore
    private String password;
    private String name;
    private SystemUserType type;
    private String phone;
    private String avatarUrl;
    private String additionalInfo;
    private Long postId;
    private BaseStatus status;
}
