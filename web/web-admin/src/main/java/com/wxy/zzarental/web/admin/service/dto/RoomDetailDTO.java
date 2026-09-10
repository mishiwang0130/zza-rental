package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.FacilityInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.entity.LeaseTerm;
import com.wxy.zzarental.model.entity.PaymentType;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class RoomDetailDTO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;

    private ApartmentInfo apartmentInfo;

    private List<GraphDTO> graphVoList;

    private List<AttrValueDTO> attrValueVoList;

    private List<FacilityInfo> facilityInfoList;

    private List<LabelInfo> labelInfoList;

    private List<PaymentType> paymentTypeList;

    private List<LeaseTerm> leaseTermList;
}
