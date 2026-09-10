package com.wxy.zzarental.web.app.controller.payment;


import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.app.service.PaymentTypeService;
import com.wxy.zzarental.web.app.service.RoomPaymentTypeService;
import com.wxy.zzarental.web.app.vo.payment.PaymentTypeRespVO;
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
    public Result<List<PaymentTypeRespVO>> list(@RequestParam Long id) {
        return Result.ok(VOConverter.toList(paymentTypeService.getPaymentTypeByRoomId(id), PaymentTypeRespVO.class));
    }

    @Operation(summary = "获取全部支付方式列表")
    @GetMapping("list")
    public Result<List<PaymentTypeRespVO>> list() {
        return Result.ok(VOConverter.toList(paymentTypeService.list(), PaymentTypeRespVO.class));
    }
}
