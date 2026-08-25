package com.wxy.zzarental.web.admin.vo.system.user;

import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.model.entity.SystemUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Schema(description = "岗位信息返回对象")
@Data
public class SystemPostItemVo extends SystemPost {
    @Schema(description = "用户信息")
    private List<SystemUser> systemUsers;


}
