package com.wxy.zzarental.web.admin.vo.system.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.wxy.zzarental.web.admin.enums.BaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Schema(description = "岗位信息返回对象")
@Data
public class SystemPostItemRespVO {

    private Long id;

    private String postCode;
    private String name;
    private String description;
    private BaseStatus status;

    @Schema(description = "用户信息")
    @JsonIgnoreProperties("postName")
    private List<SystemUserItemRespVO> systemUsers;


}
