package com.wxy.zzarental.web.admin.controller.lease;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.service.LeaseAgreementService;
import com.wxy.zzarental.web.admin.service.dto.AgreementDTO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementPageReqVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementRespVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "租约管理")
@RestController
@RequestMapping("/admin/agreement")
public class LeaseAgreementController {
    @Resource
    private LeaseAgreementService leaseAgreementService;

    @Operation(summary = "保存或修改租约信息")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody AgreementSaveReqVO reqVO) {
        leaseAgreementService.saveOrUpdate(VOConverter.to(reqVO, LeaseAgreement.class));
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询租约列表")
    @GetMapping("page")
    public Result<IPage<AgreementRespVO>> page(@RequestParam long current, @RequestParam long size, AgreementPageReqVO queryVo) {
        Page<LeaseAgreement> page = new Page<>(current, size);
        IPage<AgreementDTO> iPage = leaseAgreementService.selectPage(page, AdminApiAssembler.toQuery(queryVo));
        return Result.ok(AdminApiAssembler.toPage(iPage, AdminApiAssembler::toResponse));
    }

    @Operation(summary = "根据id查询租约信息")
    @GetMapping(value = {"", "getById"}, name = "getById")
    public Result<AgreementRespVO> getById(@RequestParam Long id) {
        AgreementDTO result= leaseAgreementService.getLeaseInfoById(id);
        return Result.ok(AdminApiAssembler.toResponse(result));
    }

    @Operation(summary = "根据id删除租约信息")
    @DeleteMapping("removeById")
    public Result<Void> removeById(@RequestParam Long id) {
        leaseAgreementService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id更新租约状态")
    @PostMapping("updateStatusById")
    public Result<Void> updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus status) {
        LambdaUpdateWrapper<LeaseAgreement> leaseAgreementLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        leaseAgreementLambdaUpdateWrapper.eq(BaseEntity::getId, id);
        leaseAgreementLambdaUpdateWrapper.set(LeaseAgreement::getStatus, status);
        leaseAgreementService.update(leaseAgreementLambdaUpdateWrapper);
        return Result.ok();
    }

}
