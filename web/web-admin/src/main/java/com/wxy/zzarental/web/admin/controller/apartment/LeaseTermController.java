package com.wxy.zzarental.web.admin.controller.apartment;


import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.entity.LeaseTerm;
import com.wxy.zzarental.web.admin.service.LeaseTermService;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "租期管理")
@RequestMapping("/admin/term")
@RestController
public class LeaseTermController {

    @Resource
    LeaseTermService leaseTermService;

    @GetMapping("list")
    @Operation(summary = "查询全部租期列表")
    public Result<List<LeaseTermRespVO>> listLeaseTerm() {
        List<LeaseTerm> list = leaseTermService.list();
        if (list.isEmpty()) {
            return Result.fail();
        }
        return Result.ok(VOConverter.toList(list, LeaseTermRespVO.class));
    }

    @PostMapping("saveOrUpdate")
    @Operation(summary = "保存或更新租期信息")
    public Result<Void> saveOrUpdate(@RequestBody LeaseTermSaveReqVO reqVO) {
        leaseTermService.saveOrUpdate(VOConverter.to(reqVO, LeaseTerm.class));
        return Result.ok();
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据ID删除租期")
    public Result<Void> deleteLeaseTermById(@RequestParam Long id) {
        leaseTermService.removeById(id);
        return Result.ok();
    }
}
