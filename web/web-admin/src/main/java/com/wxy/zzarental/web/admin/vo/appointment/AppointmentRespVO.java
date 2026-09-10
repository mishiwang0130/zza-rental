package com.wxy.zzarental.web.admin.vo.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.web.admin.enums.AppointmentStatus;
import com.wxy.zzarental.web.admin.vo.apartment.ApartmentBasicRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "预约看房信息")
public class AppointmentRespVO {

    private Long id;

    private Long userId;
    private String name;
    private String phone;
    private Long apartmentId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appointmentTime;
    private String additionalInfo;
    private AppointmentStatus appointmentStatus;

    @Schema(description = "预约公寓信息")
    private ApartmentBasicRespVO apartmentInfo;

}
