package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.model.entity.FacilityInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import java.util.List;
import lombok.Data;

@Data
public class ApartmentDetailDTO {

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

    private List<GraphDTO> graphVoList;

    private List<LabelInfo> labelInfoList;

    private List<FacilityInfo> facilityInfoList;

    private List<FeeValueDTO> feeValueVoList;

}
