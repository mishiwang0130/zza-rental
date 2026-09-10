package com.wxy.zzarental.web.admin.service.dto;

import com.wxy.zzarental.web.admin.entity.ApartmentInfo;
import com.wxy.zzarental.web.admin.enums.AppointmentStatus;
import java.util.Date;
import lombok.Data;

@Data
public class AppointmentDTO {

    private Long id;

    private Long userId;
    private String name;
    private String phone;
    private Long apartmentId;

    private Date appointmentTime;
    private String additionalInfo;
    private AppointmentStatus appointmentStatus;

    private ApartmentInfo apartmentInfo;

}
