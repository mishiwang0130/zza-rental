package com.wxy.zzarental.web.admin.vo.room;

import com.wxy.zzarental.web.admin.enums.ReleaseStatus;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;


@Data
@Schema(description = "房间信息")
public class RoomItemRespVO {

    private Long id;

    private String roomNumber;
    private BigDecimal rent;
    private Long apartmentId;
    private ReleaseStatus isRelease;

    @Schema(description = "租约结束日期")
    private Date leaseEndDate;

    @Schema(description = "当前入住状态")
    private Boolean isCheckIn;

    @Schema(description = "所属公寓信息")
    private ApartmentBasicRespVO apartmentInfo;

}
