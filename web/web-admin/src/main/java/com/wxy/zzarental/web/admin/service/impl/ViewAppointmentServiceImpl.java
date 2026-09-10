package com.wxy.zzarental.web.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.entity.ViewAppointment;
import com.wxy.zzarental.web.admin.mapper.ViewAppointmentMapper;
import com.wxy.zzarental.web.admin.service.ViewAppointmentService;
import com.wxy.zzarental.web.admin.service.dto.AppointmentDTO;
import com.wxy.zzarental.web.admin.service.query.AppointmentQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {
    @Resource
    private ViewAppointmentMapper viewAppointmentMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public IPage<AppointmentDTO> pageAppointment(Page<AppointmentDTO> page, AppointmentQuery queryVo) {
        IPage<AppointmentDTO> iPage = viewAppointmentMapper.selectPageAppointment(page, queryVo);
        return iPage;
    }
}
