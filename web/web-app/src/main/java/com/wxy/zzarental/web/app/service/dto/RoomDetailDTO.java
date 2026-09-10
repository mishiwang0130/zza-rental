package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.model.entity.FacilityInfo;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.entity.LeaseTerm;
import com.wxy.zzarental.model.entity.PaymentType;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class RoomDetailDTO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;
    private ApartmentItemDTO apartmentItemVo;
    private List<GraphDTO> graphVoList;
    private List<AttrValueDTO> attrValueVoList;
    private List<FacilityInfo> facilityInfoList;
    private List<LabelInfo> labelInfoList;
    private List<PaymentType> paymentTypeList;
    private List<FeeValueDTO> feeValueVoList;
    private List<LeaseTerm> leaseTermList;
}
