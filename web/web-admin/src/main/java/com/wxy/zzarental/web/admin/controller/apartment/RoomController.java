package com.wxy.zzarental.web.admin.controller.apartment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.RoomInfo;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.service.RoomInfoService;
import com.wxy.zzarental.web.admin.vo.room.RoomDetailVo;
import com.wxy.zzarental.web.admin.vo.room.RoomItemVo;
import com.wxy.zzarental.web.admin.vo.room.RoomQueryVo;
import com.wxy.zzarental.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {
    @Resource
    private RoomInfoService roomInfoService;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdateRoom(@RequestBody RoomSubmitVo roomSubmitVo) {
        roomInfoService.saveOrUpdateRoom(roomSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        IPage<RoomItemVo> page = roomInfoService.pageItem(current, size, queryVo);
        return Result.ok(page);
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo roomDetailVo = roomInfoService.getDetailById(id);
        return Result.ok(roomDetailVo);
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        roomInfoService.removeRoomById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(Long id, ReleaseStatus status) {
        LambdaUpdateWrapper<RoomInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<RoomInfo>();
        lambdaUpdateWrapper.eq(RoomInfo::getId, id);
        lambdaUpdateWrapper.set(RoomInfo::getIsRelease, status);
        roomInfoService.update( lambdaUpdateWrapper);
        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomInfo>> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> lambdaQueryWrapper = new LambdaQueryWrapper<RoomInfo>();
        lambdaQueryWrapper.eq(RoomInfo::getApartmentId, id);
        List<RoomInfo> list = roomInfoService.list(lambdaQueryWrapper);
        return Result.ok(list);
    }

}


















