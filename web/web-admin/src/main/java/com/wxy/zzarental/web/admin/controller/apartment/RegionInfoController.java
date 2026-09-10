package com.wxy.zzarental.web.admin.controller.apartment;


import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.util.VOConverter;
import com.wxy.zzarental.web.admin.entity.CityInfo;
import com.wxy.zzarental.web.admin.entity.DistrictInfo;
import com.wxy.zzarental.web.admin.entity.ProvinceInfo;
import com.wxy.zzarental.web.admin.service.CityInfoService;
import com.wxy.zzarental.web.admin.service.DistrictInfoService;
import com.wxy.zzarental.web.admin.service.ProvinceInfoService;
import com.wxy.zzarental.web.admin.vo.region.CityRespVO;
import com.wxy.zzarental.web.admin.vo.region.DistrictRespVO;
import com.wxy.zzarental.web.admin.vo.region.ProvinceRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "地区信息管理")
@RestController
@RequestMapping("/admin/region")
public class RegionInfoController {
    @Resource
    private ProvinceInfoService provinceInfoService;
    @Resource
    private CityInfoService cityInfoService;
    @Resource
    private DistrictInfoService districtInfoService;

    @Operation(summary = "查询省份信息列表")
    @GetMapping("province/list")
    public Result<List<ProvinceRespVO>> listProvince() {
        List<ProvinceInfo> provinceInfoList = provinceInfoService.list();
        return Result.ok(VOConverter.toList(provinceInfoList, ProvinceRespVO.class));
    }

    @Operation(summary = "根据省份id查询城市信息列表")
    @GetMapping("city/listByProvinceId")
    public Result<List<CityRespVO>> listCityInfoByProvinceId(@RequestParam Long id) {
        List<CityInfo> cityInfoList = cityInfoService.listCityInfoByProvinceId(id);
        return Result.ok(VOConverter.toList(cityInfoList, CityRespVO.class));
    }

    @GetMapping("district/listByCityId")
    @Operation(summary = "根据城市id查询区县信息")
    public Result<List<DistrictRespVO>> listDistrictInfoByCityId(@RequestParam Long id) {
        List<DistrictInfo> districtInfoList = districtInfoService.listDistrictInfoByCityId(id);
        return Result.ok(VOConverter.toList(districtInfoList, DistrictRespVO.class));
    }

}
