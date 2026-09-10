package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.web.app.enums.ReleaseStatus;
import com.wxy.zzarental.web.app.entity.LabelInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class ApartmentItemDTO {

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

    private List<LabelInfo> labelInfoList;

    private List<GraphDTO> graphVoList;

    private BigDecimal minRent;
}
