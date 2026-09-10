package com.wxy.zzarental.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.enums.ItemType;
import com.wxy.zzarental.web.admin.service.LabelInfoService;
import com.wxy.zzarental.web.admin.vo.apartment.LabelRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LabelSaveReqVO;
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
    public Result<List<LabelRespVO>> labelList(@RequestParam(required = false) ItemType type) {
        LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            queryWrapper.eq(LabelInfo::getType, type);
        }
        List<LabelInfo> labelInfoList = labelInfoService.list(queryWrapper);
        return Result.ok(VOConverter.toList(labelInfoList, LabelRespVO.class));
    }

    @Operation(summary = "新增或修改标签信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdateLabel(@RequestBody LabelSaveReqVO reqVO) {
        labelInfoService.saveOrUpdate(VOConverter.to(reqVO, LabelInfo.class));
        return Result.ok();
    }

    @Operation(summary = "根据id删除标签信息")
    @DeleteMapping("deleteById")
    public Result<Void> deleteLabelById(@RequestParam Long id) {
        labelInfoService.removeById(id);
        return Result.ok();
    }
}
