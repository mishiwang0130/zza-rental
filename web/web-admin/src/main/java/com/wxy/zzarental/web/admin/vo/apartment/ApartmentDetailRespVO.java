package com.wxy.zzarental.web.admin.vo.apartment;


import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeValueRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "公寓信息")
@Data
public class ApartmentDetailRespVO {

    private Long id;

    private String name;
    private String introduction;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    private Long provinceId;
    private String provinceName;
    private String addressDetail;
    private String latitude;
    private String longitude;
    private String phone;
    private ReleaseStatus isRelease;

    @Schema(description = "图片列表")
    private List<GraphRespVO> graphVoList;

    @Schema(description = "标签列表")
    private List<LabelRespVO> labelInfoList;

    @Schema(description = "配套列表")
    private List<FacilityRespVO> facilityInfoList;

    @Schema(description = "杂费列表")
    private List<FeeValueRespVO> feeValueVoList;

}
