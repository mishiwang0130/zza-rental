package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.web.admin.enums.BaseStatus;
import java.util.List;
import lombok.Data;

@Data
public class SystemPostItemDTO {

    private Long id;

    private String postCode;
    private String name;
    private String description;
    private BaseStatus status;

    private List<SystemUserItemDTO> systemUsers;

}
