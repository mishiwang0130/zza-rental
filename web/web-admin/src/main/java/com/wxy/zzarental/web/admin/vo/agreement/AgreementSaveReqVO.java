package com.wxy.zzarental.web.admin.vo.agreement;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.model.enums.LeaseSourceType;
import com.wxy.zzarental.model.enums.LeaseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AgreementSaveReqVO {

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
}
