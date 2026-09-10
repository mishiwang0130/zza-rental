package com.wxy.zzarental.web.admin.controller.lease;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.ViewAppointment;
import com.wxy.zzarental.model.enums.AppointmentStatus;
import com.wxy.zzarental.web.admin.service.ViewAppointmentService;
import com.wxy.zzarental.web.admin.vo.appointment.AppointmentPageReqVO;
import com.wxy.zzarental.web.admin.vo.appointment.AppointmentRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@Tag(name = "预约看房管理")
@RequestMapping("/admin/appointment")
@RestController
public class ViewAppointmentController {
    @Resource
    private ViewAppointmentService viewAppointmentService;

    @Operation(summary = "分页查询预约信息")
    @GetMapping("page")
    public Result<IPage<AppointmentRespVO>> page(@RequestParam long current, @RequestParam long size, AppointmentPageReqVO queryVo) {
        Page<AppointmentRespVO> page = new Page<>(current, size);
        IPage<AppointmentRespVO> iPage = viewAppointmentService.pageAppointment(page, queryVo);
        return Result.ok(iPage);
    }

    @Operation(summary = "根据id更新预约状态")
    @PostMapping("updateStatusById")
    public Result<Void> updateStatusById(@RequestParam Long id, @RequestParam AppointmentStatus status) {
        LambdaUpdateWrapper<ViewAppointment> appointmentLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        appointmentLambdaUpdateWrapper.eq(ViewAppointment::getId, id);
        appointmentLambdaUpdateWrapper.set(ViewAppointment::getAppointmentStatus, status);
        viewAppointmentService.update(appointmentLambdaUpdateWrapper);
        return Result.ok();
    }

}
