package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.web.app.enums.LeaseSourceType;
import com.wxy.zzarental.web.app.enums.LeaseStatus;
import lombok.Data;

import java.util.List;
import java.util.Date;
import java.math.BigDecimal;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class AgreementDetailDTO {

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
    private String apartmentName;
    private List<GraphDTO> apartmentGraphVoList;
    private String roomNumber;
    private List<GraphDTO> roomGraphVoList;
    private String paymentTypeName;
    private Integer leaseTermMonthCount;
    private String leaseTermUnit;
}
