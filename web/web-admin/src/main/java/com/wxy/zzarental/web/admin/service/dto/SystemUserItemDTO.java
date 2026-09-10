package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.model.enums.SystemUserType;
import lombok.Data;

@Data
public class SystemUserItemDTO {

    private Long id;

    private String username;
    private String name;
    private SystemUserType type;
    private String phone;
    private String avatarUrl;
    private String additionalInfo;
    private Long postId;
    private BaseStatus status;

    private String postName;

}
