package com.wxy.zzarental.web.admin.vo.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.web.admin.enums.LeaseSourceType;
import com.wxy.zzarental.web.admin.enums.LeaseStatus;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.LeaseTermRespVO;
import com.wxy.zzarental.web.admin.vo.apartment.PaymentTypeRespVO;
import com.wxy.zzarental.web.admin.vo.room.RoomBasicRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "租约信息")
public class AgreementRespVO {

    private Long id;

    private String phone;
    private String name;
    private String identificationNumber;
    private Long apartmentId;
    private Long roomId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaseEndDate;
    private Long leaseTermId;
    private BigDecimal rent;
    private BigDecimal deposit;
    private Long paymentTypeId;
    private LeaseStatus status;
    private LeaseSourceType sourceType;
    private String additionalInfo;

    @Schema(description = "签约公寓信息")
    private ApartmentBasicRespVO apartmentInfo;

    @Schema(description = "签约房间信息")
    private RoomBasicRespVO roomInfo;

    @Schema(description = "支付方式")
    private PaymentTypeRespVO paymentType;

    @Schema(description = "租期")
    private LeaseTermRespVO leaseTerm;
}
