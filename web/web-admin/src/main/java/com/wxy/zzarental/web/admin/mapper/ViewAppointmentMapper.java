package com.wxy.zzarental.web.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxy.zzarental.model.entity.ViewAppointment;
import com.wxy.zzarental.web.admin.service.dto.AppointmentDTO;
import com.wxy.zzarental.web.admin.service.query.AppointmentQuery;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.wxy.zzarental.model.ViewAppointment
*/
public interface ViewAppointmentMapper extends BaseMapper<ViewAppointment> {

    IPage<AppointmentDTO> selectPageAppointment(Page<AppointmentDTO> page, AppointmentQuery queryVo);
}
