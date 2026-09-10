package com.wxy.zzarental.web.admin.service.command;

import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.service.dto.GraphDTO;
import java.util.List;
import lombok.Data;

@Data
public class ApartmentSaveCommand {

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

    private List<Long> facilityInfoIds;

    private List<Long> labelIds;

    private List<Long> feeValueIds;

    private List<GraphDTO> graphVoList;

}
