package com.wxy.zzarental.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.web.admin.service.LabelInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/admin/label")
public class LabelController {

    @Resource
    LabelInfoService labelInfoService;
    @Operation(summary = "（根据类型）查询标签列表")
    @GetMapping("list")
    public Result<List<LabelInfo>> labelList(@RequestParam(required = false) ItemType type) {
        System.out.println(type);
        LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            queryWrapper.eq(LabelInfo::getType, type);
        }
        List<LabelInfo> labelInfoList = labelInfoService.list(queryWrapper);
        return Result.ok(labelInfoList);
    }

    @Operation(summary = "新增或修改标签信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdateLabel(@RequestBody LabelInfo labelInfo) {
        labelInfoService.saveOrUpdate(labelInfo);
        return Result.ok();
    }

    @Operation(summary = "根据id删除标签信息")
    @DeleteMapping("deleteById")
    public Result deleteLabelById(@RequestParam Long id) {
        labelInfoService.removeById(id);
        return Result.ok();
    }
}
