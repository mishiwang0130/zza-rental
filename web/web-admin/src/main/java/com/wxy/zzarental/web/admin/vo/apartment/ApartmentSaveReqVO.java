package com.wxy.zzarental.web.admin.vo.apartment;


import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.vo.graph.GraphReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "公寓信息")
@Data
public class ApartmentSaveReqVO {

    @Schema(description = "公寓ID，更新时传入")
    private Long id;

    @Schema(description = "公寓名称")
    private String name;

    @Schema(description = "公寓介绍")
    private String introduction;

    @Schema(description = "区县ID")
    private Long districtId;

    @Schema(description = "区县名称")
    private String districtName;

    @Schema(description = "城市ID")
    private Long cityId;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "省份ID")
    private Long provinceId;

    @Schema(description = "省份名称")
    private String provinceName;

    @Schema(description = "详细地址")
    private String addressDetail;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "发布状态")
    private ReleaseStatus isRelease;

    @Schema(description="公寓配套id")
    private List<Long> facilityInfoIds;

    @Schema(description="公寓标签id")
    private List<Long> labelIds;

    @Schema(description="公寓杂费值id")
    private List<Long> feeValueIds;

    @Schema(description="公寓图片id")
    private List<GraphReqVO> graphVoList;

}
