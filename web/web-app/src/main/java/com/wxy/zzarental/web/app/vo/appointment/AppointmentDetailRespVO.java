package com.wxy.zzarental.web.app.vo.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.app.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


@Data
@Schema(description = "APP端预约看房详情")
public class AppointmentDetailRespVO {

    private Long id;

    private Long userId;
    private String name;
    private String phone;
    private Long apartmentId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appointmentTime;
    private String additionalInfo;
    private AppointmentStatus appointmentStatus;

    @Schema(description = "公寓基本信息")
    private ApartmentItemRespVO apartmentItemVo;
}
