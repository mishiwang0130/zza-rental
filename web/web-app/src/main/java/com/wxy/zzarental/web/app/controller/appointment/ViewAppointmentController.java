package com.wxy.zzarental.web.app.controller.appointment;


import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.app.entity.ViewAppointment;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.wxy.zzarental.web.app.service.ViewAppointmentService;
import com.wxy.zzarental.web.app.controller.assembler.AppApiAssembler;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentDetailRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentItemRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看房预约信息")
@RestController
@RequestMapping("/app/appointment")
public class ViewAppointmentController {

    @Resource
    private ViewAppointmentService viewAppointmentService;
    @Resource
    private ApartmentInfoService apartmentInfoService;

    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody AppointmentSaveReqVO reqVO) {
        ViewAppointment viewAppointment = VOConverter.to(reqVO, ViewAppointment.class);
        viewAppointmentService.saveOrUpdateViewAppointment(viewAppointment);
        return Result.ok();
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemRespVO>> listItem() {
        List<AppointmentItemRespVO> list = AppApiAssembler.toAppointmentList(viewAppointmentService.listItem());
        return Result.ok(list);
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailRespVO> getDetailById(Long id) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        AppointmentDetailRespVO appointmentDetailVo = AppApiAssembler.toResponse(viewAppointmentService.getDetailById(id, userId));

        return Result.ok(appointmentDetailVo);
    }

}

