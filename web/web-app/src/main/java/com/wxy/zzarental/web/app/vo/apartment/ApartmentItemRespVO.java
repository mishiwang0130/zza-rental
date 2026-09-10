package com.wxy.zzarental.web.app.vo.apartment;


import com.wxy.zzarental.model.enums.ReleaseStatus;
import com.wxy.zzarental.web.app.vo.common.LabelRespVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "App端公寓信息")
public class ApartmentItemRespVO {

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

    private List<LabelRespVO> labelInfoList;

    private List<GraphRespVO> graphVoList;

    private BigDecimal minRent;
}
