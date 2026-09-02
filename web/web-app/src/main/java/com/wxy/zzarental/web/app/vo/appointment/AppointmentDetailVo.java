package com.wxy.zzarental.web.app.vo.appointment;

import com.wxy.zzarental.model.entity.ViewAppointment;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "APP端预约看房详情")
public class AppointmentDetailVo extends ViewAppointment {

    @Schema(description = "公寓基本信息")
    private ApartmentItemVo apartmentItemVo;
}
