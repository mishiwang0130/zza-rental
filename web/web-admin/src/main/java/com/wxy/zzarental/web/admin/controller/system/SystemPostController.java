package com.wxy.zzarental.web.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.service.SystemPostService;
import com.wxy.zzarental.web.admin.service.dto.SystemPostItemDTO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "后台用户岗位管理")
@RequestMapping("/admin/system/post")
public class SystemPostController {

    @Resource
    private SystemPostService systemPostService;

    @Operation(summary = "分页获取岗位信息")
    @GetMapping("page")
    public Result<IPage<SystemPostRespVO>> page(@RequestParam long current, @RequestParam long size) {
        IPage<SystemPost> systemPostPage = new Page<>(current, size);
        IPage<SystemPost> page = systemPostService.page(systemPostPage);
        return Result.ok(page.convert(item -> VOConverter.to(item, SystemPostRespVO.class)));
    }

    @Operation(summary = "保存或更新岗位信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody SystemPostSaveReqVO reqVO) {
        systemPostService.saveOrUpdate(VOConverter.to(reqVO, SystemPost.class));
        return Result.ok();
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据id删除岗位")
    public Result<Void> removeById(@RequestParam Long id) {
        systemPostService.removeById(id);

        return Result.ok();
    }

    @GetMapping("getById")
    @Operation(summary = "根据id获取岗位信息")
    public Result<SystemPostRespVO> getById(@RequestParam Long id) {
        return Result.ok(VOConverter.to(systemPostService.getById(id), SystemPostRespVO.class));
    }

    @Operation(summary = "获取全部岗位列表")
    @GetMapping("list")
    public Result<Void> list() {
        systemPostService.list();
        return Result.ok();
    }

    @Operation(summary = "根据岗位id修改状态")
    @PostMapping("updateStatusByPostId")
    public Result<Void> updateStatusByPostId(@RequestParam Long id, @RequestParam BaseStatus status) {
        // 这里是根据id来修改
        LambdaUpdateWrapper<SystemPost> systemPostLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        systemPostLambdaUpdateWrapper.eq(BaseEntity::getId, id);
        systemPostLambdaUpdateWrapper.set(SystemPost::getStatus, status);
        systemPostService.update(systemPostLambdaUpdateWrapper);
        // selectById和removeById是传id去做操作，updateById是传一个实体，他会把id当条件，把其他“非空”字段全部set一次
        // 通过updateById去做修改的时候，不会把null值的字段拼接到这条update语句的set上
//        SystemPost systemPost = new SystemPost();
//        systemPost.setId(id);
//        systemPost.setName("aaa");
//        systemPost.setStatus(status);
//        systemPostService.updateById(systemPost);
        return Result.ok();
    }

    /**
     * 岗位信息分页，可以传入岗位名称进行模糊擦好像，每条记录不仅要有岗位信息，还要有该岗位下的人员信息
     *
     * @param current 当前页码
     * @param size 每页条数
     * @param postName 岗位名称
     * @return 岗位及关联用户分页结果
     * @author wxy
     * @date 2026/08/25
     */
    @Operation(summary = "分页获取岗位信息")
    @GetMapping("page1")
    public Result<IPage<SystemPostItemRespVO>> page1(@RequestParam long current, @RequestParam long size, @RequestParam String postName) {
        IPage<SystemPost> systemPostPage = new Page<>(current, size);
        IPage<SystemPostItemDTO> page = systemPostService.page1(systemPostPage, postName);
        return Result.ok(AdminApiAssembler.toPage(page, AdminApiAssembler::toResponse));
    }
}
