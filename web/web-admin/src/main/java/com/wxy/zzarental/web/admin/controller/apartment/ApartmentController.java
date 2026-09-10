package com.wxy.zzarental.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.service.ApartmentInfoService;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentDetailRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentSaveReqVO;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "公寓信息管理")
@RestController
@RequestMapping("/admin/apartment")
public class ApartmentController {
    @Resource
    private ApartmentInfoService apartmentInfoService;

    @Operation(summary = "保存或更新公寓信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdateApart(@RequestBody ApartmentSaveReqVO reqVO) {
        apartmentInfoService.saveOrUpdateApart(reqVO);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询公寓列表")
    @GetMapping("pageItem")
    public Result<IPage<ApartmentItemRespVO>> pageItem(@RequestParam long current, @RequestParam long size, ApartmentPageReqVO queryVo) {
        Page<ApartmentItemRespVO> page = new Page<>(current, size);
        IPage<ApartmentItemRespVO> iPage = apartmentInfoService.pageItem(page, queryVo);
        return Result.ok(iPage);
    }

    @Operation(summary = "根据ID获取公寓详细信息")
    @GetMapping("getDetailById")
    public Result<ApartmentDetailRespVO> getDetailById(@RequestParam Long id) {
        ApartmentDetailRespVO result = apartmentInfoService.getDetailById(id);
        return Result.ok(result);
    }

    @Operation(summary = "根据id删除公寓信息")
    @DeleteMapping("removeById")
    public Result<Void> removeApartmentById(@RequestParam Long id) {
        apartmentInfoService.removeApartmentById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改公寓发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result<Void> updateReleaseStatusById(@RequestParam Long id, @RequestParam ReleaseStatus status) {
        LambdaUpdateWrapper<ApartmentInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApartmentInfo::getId, id);
        updateWrapper.set(ApartmentInfo::getIsRelease, status);
        apartmentInfoService.update(updateWrapper);
        return Result.ok();
    }

    @Operation(summary = "根据区县id查询公寓信息列表")
    @GetMapping("listInfoByDistrictId")
    public Result<List<ApartmentBasicRespVO>> listInfoByDistrictId(@RequestParam Long id) {
        LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApartmentInfo::getDistrictId, id);
        List<ApartmentInfo> list = apartmentInfoService.list(queryWrapper);
        return Result.ok(VOConverter.toList(list, ApartmentBasicRespVO.class));
    }
}














