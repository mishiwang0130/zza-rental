package com.wxy.zzarental.web.app.vo.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.wxy.zzarental.web.app.enums.AppointmentStatus;
import lombok.Data;

import java.util.Date;

@Data
public class AppointmentSaveReqVO {

    private Long id;
    private Long userId;

    private String name;

    private String phone;

    private Long apartmentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appointmentTime;

    private String additionalInfo;
    private AppointmentStatus appointmentStatus;
}
