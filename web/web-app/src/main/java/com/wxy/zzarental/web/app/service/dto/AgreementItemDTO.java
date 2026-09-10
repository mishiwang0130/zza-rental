package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.web.app.enums.LeaseSourceType;
import com.wxy.zzarental.web.app.enums.LeaseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class AgreementItemDTO {
    private Long id;
    private List<GraphDTO> roomGraphVoList;
    private String apartmentName;
    private String roomNumber;
    private LeaseStatus leaseStatus;
    private Date leaseStartDate;
    private Date leaseEndDate;
    private LeaseSourceType sourceType;
    private BigDecimal rent;

}
