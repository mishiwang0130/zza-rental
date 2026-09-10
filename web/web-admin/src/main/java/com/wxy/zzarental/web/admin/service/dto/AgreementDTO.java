package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.web.admin.entity.ApartmentInfo;
import com.wxy.zzarental.web.admin.entity.LeaseTerm;
import com.wxy.zzarental.web.admin.entity.PaymentType;
import com.wxy.zzarental.web.admin.entity.RoomInfo;
import com.wxy.zzarental.web.admin.enums.LeaseSourceType;
import com.wxy.zzarental.web.admin.enums.LeaseStatus;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class AgreementDTO {

    private Long id;

    private String phone;
    private String name;
    private String identificationNumber;
    private Long apartmentId;
    private Long roomId;

    private Date leaseStartDate;

    private Date leaseEndDate;
    private Long leaseTermId;
    private BigDecimal rent;
    private BigDecimal deposit;
    private Long paymentTypeId;
    private LeaseStatus status;
    private LeaseSourceType sourceType;
    private String additionalInfo;

    private ApartmentInfo apartmentInfo;

    private RoomInfo roomInfo;

    private PaymentType paymentType;

    private LeaseTerm leaseTerm;
}
