package com.wxy.zzarental.web.admin.vo.system.user;

import com.wxy.zzarental.web.admin.enums.BaseStatus;
import lombok.Data;

@Data
public class SystemPostRespVO {
    private Long id;
    private String postCode;
    private String name;
    private String description;
    private BaseStatus status;
}
