package com.wxy.zzarental.web.admin.controller.apartment;


import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.PaymentType;
import com.wxy.zzarental.web.admin.service.PaymentTypeService;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "支付方式管理")
@RequestMapping("/admin/payment")
@RestController
public class PaymentTypeController {

    @Resource
    PaymentTypeService paymentTypeService;

    @Operation(summary = "查询全部支付方式列表")
    @GetMapping("list")
    public Result<List<PaymentTypeRespVO>> listPaymentType() {
        List<PaymentType> paymentTypes = paymentTypeService.list();
        return Result.ok(VOConverter.toList(paymentTypes, PaymentTypeRespVO.class));
    }

    @Operation(summary = "保存或更新支付方式")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdatePaymentType(@RequestBody PaymentTypeSaveReqVO reqVO) {
        paymentTypeService.saveOrUpdate(VOConverter.to(reqVO, PaymentType.class));
        return Result.ok();
    }

    @Operation(summary = "根据ID删除支付方式")
    @DeleteMapping("deleteById")
    public Result<Void> deletePaymentById(@RequestParam Long id) {
        paymentTypeService.removeById(id);
        return Result.ok();
    }

}















