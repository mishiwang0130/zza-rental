package com.wxy.zzarental.web.admin.controller.apartment;


import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.model.entity.AttrValue;
import com.wxy.zzarental.web.admin.service.AttrKeyService;
import com.wxy.zzarental.web.admin.service.AttrValueService;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeyRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeySaveReqVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrValueSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "房间属性管理")
@RestController
@RequestMapping("/admin/attr")
public class AttrController {
    @Resource
    private AttrKeyService attrKeyService;
    @Resource
    private AttrValueService attrValueService;

    @Operation(summary = "新增或更新属性名称")
    @PostMapping("key/saveOrUpdate")
    public Result<Void> saveOrUpdateAttrKey(@RequestBody AttrKeySaveReqVO reqVO) {
        attrKeyService.saveOrUpdate(VOConverter.to(reqVO, AttrKey.class));
        return Result.ok();
    }

    @Operation(summary = "新增或更新属性值")
    @PostMapping("value/saveOrUpdate")
    public Result<Void> saveOrUpdateAttrValue(@RequestBody List<AttrValueSaveReqVO> reqVOList) {
        attrValueService.saveOrUpdateBatch(VOConverter.toList(reqVOList, AttrValue.class));
        return Result.ok();
    }


    @Operation(summary = "查询全部属性名称和属性值列表")
    @GetMapping("list")
    public Result<List<AttrKeyRespVO>> listAttrInfo() {
        List<AttrKeyRespVO> attrKeyVoList = attrKeyService.listAttrInfo();
        return Result.ok(attrKeyVoList);
    }

    @Operation(summary = "根据id删除属性名称")
    @DeleteMapping("key/deleteById")
    public Result<Void> removeAttrKeyById(@RequestParam Long attrKeyId) {
        attrKeyService.removeAttrKeyById(attrKeyId);
        return Result.ok();
    }

    @Operation(summary = "根据id删除属性值")
    @DeleteMapping("value/deleteById")
    public Result<Void> removeAttrValueById(@RequestParam Long id) {
        attrValueService.removeById(id);
        return Result.ok();
    }

}
