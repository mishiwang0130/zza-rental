package com.wxy.zzarental.web.app.controller.agreement;

import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.LeaseAgreement;
import com.wxy.zzarental.model.enums.LeaseStatus;
import com.wxy.zzarental.web.app.service.LeaseAgreementService;
import com.wxy.zzarental.web.app.vo.agreement.AgreementDetailRespVO;
import com.wxy.zzarental.web.app.vo.agreement.AgreementItemRespVO;
import com.wxy.zzarental.web.app.vo.agreement.AgreementSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/agreement")
@Tag(name = "租约信息")
public class LeaseAgreementController {

    @Resource
    private LeaseAgreementService leaseAgreementService;

    @Operation(summary = "获取个人租约基本信息列表")
    @GetMapping("listItem")
    public Result<List<AgreementItemRespVO>> listItem() {
        String phone = LoginUserHolder.getLoginUser().getUserName();
        List<AgreementItemRespVO> list = leaseAgreementService.listItemByPhone(phone);
        return Result.ok(list);
    }

    @Operation(summary = "根据id获取租约详细信息")
    @GetMapping("getDetailById")
    public Result<AgreementDetailRespVO> getDetailById(@RequestParam Long id) {
        AgreementDetailRespVO agreementDetailVo = leaseAgreementService.getDetailById(id);
        return Result.ok(agreementDetailVo);
    }

    @Operation(summary = "根据id更新租约状态", description = "用于确认租约和提前退租")
    @PostMapping("updateStatusById")
    public Result<Void> updateStatusById(@RequestParam Long id, @RequestParam Integer leaseStatus) {
        LeaseAgreement leaseAgreement = leaseAgreementService.getById(id);
        if (leaseAgreement != null) {
            leaseAgreement.setStatus(LeaseStatus.isExistById(leaseStatus));
            leaseAgreementService.updateById(leaseAgreement);
        }
        return Result.ok();
    }

    @Operation(summary = "保存或更新租约", description = "用于续约")
    @PostMapping("saveOrUpdate")
    public Result<Void> saveOrUpdate(@RequestBody AgreementSaveReqVO reqVO) {
        leaseAgreementService.saveOrUpdateLeaseAgreement(VOConverter.to(reqVO, LeaseAgreement.class));
        return Result.ok();
    }

}
