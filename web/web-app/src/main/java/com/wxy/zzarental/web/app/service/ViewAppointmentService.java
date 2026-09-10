package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.ViewAppointment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentDetailVo;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentItemVo;

import java.util.List;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface ViewAppointmentService extends IService<ViewAppointment> {
    List<AppointmentItemVo> listItem();

    void saveOrUpdateViewAppointment(ViewAppointment viewAppointment);

    AppointmentDetailVo getDetailById(Long id,Long userId);
}
