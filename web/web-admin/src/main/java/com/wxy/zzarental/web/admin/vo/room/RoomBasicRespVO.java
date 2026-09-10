package com.wxy.zzarental.web.admin.vo.room;

import com.wxy.zzarental.model.enums.ReleaseStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomBasicRespVO {

    private Long id;
    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;
}
