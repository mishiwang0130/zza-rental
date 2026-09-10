package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.enums.ReleaseStatus;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class RoomItemDTO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;

    private Date leaseEndDate;

    private Boolean isCheckIn;

    private ApartmentInfo apartmentInfo;

}
