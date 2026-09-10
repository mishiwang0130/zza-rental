package com.wxy.zzarental.web.app.controller.room;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.web.app.service.RoomInfoService;
import com.wxy.zzarental.web.app.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomPageReqVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "房间信息")
@RestController
@RequestMapping("/app/room")
public class RoomController {
    @Resource
    private RoomInfoService roomInfoService;

    @Operation(summary = "分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemRespVO>> pageItem(@RequestParam long current, @RequestParam long size, RoomPageReqVO queryVo) {
        Page<RoomItemRespVO> page = new Page<>(current, size);
        IPage<RoomItemRespVO> result = roomInfoService.pageItem(page, queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据id获取房间的详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailRespVO> getDetailById(@RequestParam Long id) {
        RoomDetailRespVO result = roomInfoService.getDetailById(id);
        return Result.ok(result);
    }

    @Operation(summary = "根据公寓id分页查询房间列表")
    @GetMapping("pageItemByApartmentId")
    public Result<IPage<RoomItemRespVO>> pageItemByApartmentId(@RequestParam long current, @RequestParam long size, @RequestParam Long id) {
        Page<RoomItemRespVO> page = new Page<>(current, size);
        IPage<RoomItemRespVO> result = roomInfoService.pageItemByApartmentId(page, id);
        return Result.ok(result);
    }

}
