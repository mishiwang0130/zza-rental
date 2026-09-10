package com.wxy.zzarental.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.entity.FacilityInfo;
import com.wxy.zzarental.web.admin.enums.ItemType;
import com.wxy.zzarental.web.admin.service.FacilityInfoService;
import com.wxy.zzarental.web.admin.vo.apartment.FacilityRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.FacilitySaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "配套管理")
@RestController
@RequestMapping("/admin/facility")
public class FacilityController {
    @Resource
    private FacilityInfoService facilityInfoService;

    @Operation(summary = "[根据类型]查询配套信息列表")
    @GetMapping("list")
    public Result<List<FacilityRespVO>> listFacility(@RequestParam(required = false) ItemType type) {
        LambdaQueryWrapper<FacilityInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            queryWrapper.eq(FacilityInfo::getType, type);
        }
        List<FacilityInfo> list = facilityInfoService.list(queryWrapper);
        return Result.ok(VOConverter.toList(list, FacilityRespVO.class));
    }

    @Operation(summary = "新增或修改配套信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody FacilitySaveReqVO reqVO) {
        facilityInfoService.saveOrUpdate(VOConverter.to(reqVO, FacilityInfo.class));
        return Result.ok();
    }

    @Operation(summary = "根据id删除配套信息")
    @DeleteMapping("deleteById")
    public Result<Void> removeFacilityById(@RequestParam Long id) {
        facilityInfoService.removeById(id);
        return Result.ok();
    }

}
