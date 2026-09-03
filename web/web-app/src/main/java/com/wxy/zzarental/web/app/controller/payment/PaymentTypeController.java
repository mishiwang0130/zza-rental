package com.wxy.zzarental.web.app.controller.payment;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.PaymentType;
import com.wxy.zzarental.model.entity.RoomPaymentType;
import com.wxy.zzarental.web.app.service.PaymentTypeService;
import com.wxy.zzarental.web.app.service.RoomPaymentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "支付方式接口")
@RestController
@RequestMapping("/app/payment")
public class PaymentTypeController {
    @Resource
    private PaymentTypeService paymentTypeService;
    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Operation(summary = "根据房间id获取可选支付方式列表")
    @GetMapping("listByRoomId")
    public Result<List<PaymentType>> list(@RequestParam Long id) {
        return Result.ok(paymentTypeService.getPaymentTypeByRoomId(id));
    }

    @Operation(summary = "获取全部支付方式列表")
    @GetMapping("list")
    public Result<List<PaymentType>> list() {
        return Result.ok(paymentTypeService.list());
    }
}
