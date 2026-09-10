package com.wxy.zzarental.web.admin.controller.system;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.admin.service.SystemUserService;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserPageReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserSaveReqVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "后台用户信息管理")
@RestController
@RequestMapping("/admin/system/user")
public class SystemUserController {

    @Autowired
    @Resource
    private SystemUserService systemUserService;

    @Operation(summary = "根据条件分页查询后台用户列表")
    @GetMapping("page")
    public Result<IPage<SystemUserItemRespVO>> page(@RequestParam long current, @RequestParam long size, SystemUserPageReqVO queryVo) {
        Page<SystemUser> systemUserPage = new Page<>(current, size);
        IPage<SystemUserItemRespVO> result = systemUserService.pageUser(systemUserPage, queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据ID查询后台用户信息")
    @GetMapping("getById")
    public Result<SystemUserItemRespVO> getById(@RequestParam Long id) {
        SystemUserItemRespVO result = systemUserService.getSystemUserById(id);
        return Result.ok(result);
    }

    @Operation(summary = "保存或更新后台用户信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody SystemUserSaveReqVO reqVO) {
        SystemUser systemUser = VOConverter.to(reqVO, SystemUser.class);
        if (BeanUtil.isNotEmpty(systemUser.getPassword())) {
            systemUser.setPassword(DigestUtils.md5Hex(systemUser.getPassword()));
        }
        systemUserService.saveOrUpdate(systemUser);
        return Result.ok();
    }

    @Operation(summary = "判断后台用户名是否可用")
    @GetMapping("isUserNameAvailable")
    public Result<Boolean> isUsernameExists(@RequestParam String username) {
        LambdaQueryWrapper<SystemUser> systemUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        systemUserLambdaQueryWrapper.eq(SystemUser::getUsername, username);
        long count = systemUserService.count(systemUserLambdaQueryWrapper);

        return Result.ok(count == 0);
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据ID删除后台用户信息")
    public Result<Void> removeById(@RequestParam Long id) {
        systemUserService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据ID修改后台用户状态")
    @PostMapping("updateStatusByUserId")
    public Result<Void> updateStatusByUserId(@RequestParam Long id, @RequestParam BaseStatus status) {
        LambdaUpdateWrapper<SystemUser> systemUserLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        systemUserLambdaUpdateWrapper.eq(BaseEntity::getId, id);
        systemUserLambdaUpdateWrapper.set(SystemUser::getStatus, status);
        systemUserService.update(systemUserLambdaUpdateWrapper);
        return Result.ok();
    }


}
