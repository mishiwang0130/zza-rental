package com.wxy.zzarental.web.app.controller.appointment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.ViewAppointment;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.wxy.zzarental.web.app.service.ViewAppointmentService;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentDetailVo;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentItemVo;
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
    public Result<Void> saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        viewAppointmentService.saveOrUpdateViewAppointment(viewAppointment);
        return Result.ok();
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        List<AppointmentItemVo> list = viewAppointmentService.listItem();
        return Result.ok(list);
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(Long id) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        AppointmentDetailVo appointmentDetailVo = viewAppointmentService.getDetailById(id,userId);

        return Result.ok(appointmentDetailVo);
    }

}

