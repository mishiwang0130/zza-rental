package com.wxy.zzarental.web.admin.controller.apartment;

import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.entity.FeeKey;
import com.wxy.zzarental.web.admin.entity.FeeValue;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.service.FeeKeyService;
import com.wxy.zzarental.web.admin.service.FeeValueService;
import com.wxy.zzarental.web.admin.service.dto.FeeKeyDTO;
import com.wxy.zzarental.web.admin.vo.fee.FeeKeyRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeKeySaveReqVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeValueSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "房间杂费管理")
@RestController
@RequestMapping("/admin/fee")
public class FeeController {

    @Resource
    private FeeKeyService feeKeyService;
    @Resource
    private FeeValueService feeValueService;

    @Operation(summary = "保存或更新杂费名称")
    @PostMapping("key/saveOrUpdate")
    public Result<Void> saveOrUpdateFeeKey(@RequestBody FeeKeySaveReqVO reqVO) {
        feeKeyService.saveOrUpdate(VOConverter.to(reqVO, FeeKey.class));
        return Result.ok();
    }

    @Operation(summary = "保存或更新杂费值")
    @PostMapping("value/saveOrUpdate")
    public Result<Void> saveOrUpdateFeeValue(@RequestBody FeeValueSaveReqVO reqVO) {
        feeValueService.saveOrUpdate(VOConverter.to(reqVO, FeeValue.class));
        return Result.ok();
    }

    @Operation(summary = "查询全部杂费名称和杂费值列表")
    @GetMapping("list")
    public Result<List<FeeKeyRespVO>> feeInfoList() {
        List<FeeKeyDTO> feeKeyVoList = feeKeyService.feeInfoList();
        return Result.ok(AdminApiAssembler.toList(feeKeyVoList, AdminApiAssembler::toResponse));
    }

    @Operation(summary = "根据id删除杂费名称")
    @DeleteMapping("key/deleteById")
    public Result<Void> deleteFeeKeyById(@RequestParam Long feeKeyId) {
        feeKeyService.deleteFeeKeyById(feeKeyId);
        return Result.ok();
    }

    @Operation(summary = "根据id删除杂费值")
    @DeleteMapping("value/deleteById")
    public Result<Void> deleteFeeValueById(@RequestParam Long id) {
        feeValueService.removeById(id);
        return Result.ok();
    }
}
