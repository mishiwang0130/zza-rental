package com.wxy.zzarental.web.admin.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.UserInfo;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.admin.service.UserInfoService;
import com.wxy.zzarental.web.admin.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户信息管理")
@RestController
@RequestMapping("/admin/user")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @Operation(summary = "分页查询用户信息")
    @GetMapping("page")
    public Result<IPage<UserInfo>> pageUserInfo(@RequestParam long current, @RequestParam long size, UserInfoQueryVo queryVo) {
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(queryVo.getStatus()!=null,UserInfo::getStatus,queryVo.getStatus());
        userInfoLambdaQueryWrapper.like(queryVo.getPhone()!= null,UserInfo::getPhone,queryVo.getPhone());
        IPage<UserInfo> userInfoPage = new Page<>(current,size);
        IPage<UserInfo> result = userInfoService.page(userInfoPage, userInfoLambdaQueryWrapper);
        return Result.ok(result);
    }

    @Operation(summary = "根据用户id更新账号状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam BaseStatus status) {
        LambdaUpdateWrapper<UserInfo> userInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userInfoLambdaUpdateWrapper.eq(BaseEntity::getId,id);
        userInfoLambdaUpdateWrapper.set(UserInfo::getStatus,status);
        userInfoService.update(userInfoLambdaUpdateWrapper);
        return Result.ok();
    }
}
