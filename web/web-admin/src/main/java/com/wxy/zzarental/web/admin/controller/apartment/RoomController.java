package com.wxy.zzarental.web.admin.controller.apartment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.entity.RoomInfo;
import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.service.RoomInfoService;
import com.wxy.zzarental.web.admin.service.dto.RoomDetailDTO;
import com.wxy.zzarental.web.admin.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.admin.vo.room.RoomBasicRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomPageReqVO;
import com.wxy.zzarental.web.admin.vo.room.RoomSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {
    @Resource
    private RoomInfoService roomInfoService;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdateRoom(@RequestBody RoomSaveReqVO reqVO) {
        roomInfoService.saveOrUpdateRoom(AdminApiAssembler.toCommand(reqVO));
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemRespVO>> pageItem(@RequestParam long current, @RequestParam long size, RoomPageReqVO queryVo) {
        IPage<RoomItemDTO> page = roomInfoService.pageItem(current, size, AdminApiAssembler.toQuery(queryVo));
        return Result.ok(AdminApiAssembler.toPage(page, AdminApiAssembler::toResponse));
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailRespVO> getDetailById(@RequestParam Long id) {
        RoomDetailDTO roomDetailVo = roomInfoService.getDetailById(id);
        return Result.ok(AdminApiAssembler.toResponse(roomDetailVo));
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result<Void> removeById(@RequestParam Long id) {
        roomInfoService.removeRoomById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result<Void> updateReleaseStatusById(Long id, ReleaseStatus status) {
        LambdaUpdateWrapper<RoomInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<RoomInfo>();
        lambdaUpdateWrapper.eq(RoomInfo::getId, id);
        lambdaUpdateWrapper.set(RoomInfo::getIsRelease, status);
        roomInfoService.update( lambdaUpdateWrapper);
        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomBasicRespVO>> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> lambdaQueryWrapper = new LambdaQueryWrapper<RoomInfo>();
        lambdaQueryWrapper.eq(RoomInfo::getApartmentId, id);
        List<RoomInfo> list = roomInfoService.list(lambdaQueryWrapper);
        return Result.ok(VOConverter.toList(list, RoomBasicRespVO.class));
    }

}
