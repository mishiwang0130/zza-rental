package com.wxy.zzarental.web.admin.vo.system.user;

import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.model.enums.SystemUserType;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "后台管理系统用户基本信息实体")
public class SystemUserItemRespVO {

    private Long id;

    private String username;
    private String name;
    private SystemUserType type;
    private String phone;
    private String avatarUrl;
    private String additionalInfo;
    private Long postId;
    private BaseStatus status;

    @Schema(description = "岗位名称")
    @TableField(value = "post_name")
    private String postName;

}
