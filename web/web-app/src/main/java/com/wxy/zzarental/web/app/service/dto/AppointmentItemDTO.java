package com.wxy.zzarental.web.app.service.dto;

import com.wxy.zzarental.web.app.enums.AppointmentStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

/** APP 服务内部模型，与 HTTP VO 的序列化契约独立。 */
@Data
public class AppointmentItemDTO {
    private Long id;
    private String apartmentName;
    private List<GraphDTO> graphVoList;
    private Date appointmentTime;
    private AppointmentStatus appointmentStatus;
}
