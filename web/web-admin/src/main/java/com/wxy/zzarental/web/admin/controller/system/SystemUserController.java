package com.wxy.zzarental.web.admin.controller.system;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.admin.service.SystemUserService;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserItemVo;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@Tag(name = "后台用户信息管理")
@RestController
@RequestMapping("/admin/system/user")
public class SystemUserController {

    @Resource
    private SystemUserService systemUserService;

    @Operation(summary = "根据条件分页查询后台用户列表")
    @GetMapping("page")
    public Result<IPage<SystemUserItemVo>> page(@RequestParam long current, @RequestParam long size, SystemUserQueryVo queryVo) {
        Page<SystemUser> systemUserPage = new Page<>(current, size);
        // service也要改呀
        IPage<SystemUserItemVo> result = systemUserService.pageUser(systemUserPage,queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据ID查询后台用户信息")
    @GetMapping("getById")
    public Result<SystemUserItemVo> getById(@RequestParam Long id) {
        SystemUserItemVo result = systemUserService.getSystemUserById(id);
        return Result.ok(result);
    }

    @Operation(summary = "保存或更新后台用户信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody SystemUser systemUser) {
        return Result.ok();
    }

    @Operation(summary = "判断后台用户名是否可用")
    @GetMapping("isUserNameAvailable")
    public Result<Boolean> isUsernameExists(@RequestParam String username) {
        return Result.ok();
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据ID删除后台用户信息")
    public Result removeById(@RequestParam Long id) {
        return Result.ok();
    }

    @Operation(summary = "根据ID修改后台用户状态")
    @PostMapping("updateStatusByUserId")
    public Result updateStatusByUserId(@RequestParam Long id, @RequestParam BaseStatus status) {
        return Result.ok();
    }


}
