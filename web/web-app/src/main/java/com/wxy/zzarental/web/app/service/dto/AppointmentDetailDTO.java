package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.model.enums.AppointmentStatus;
import lombok.Data;

import java.util.Date;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class AppointmentDetailDTO {

    private Long id;

    private Long userId;
    private String name;
    private String phone;
    private Long apartmentId;
    private Date appointmentTime;
    private String additionalInfo;
    private AppointmentStatus appointmentStatus;
    private ApartmentItemDTO apartmentItemVo;
}
