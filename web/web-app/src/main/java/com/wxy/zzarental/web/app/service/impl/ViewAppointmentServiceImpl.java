package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.web.app.entity.ApartmentInfo;
import com.wxy.zzarental.web.app.entity.BaseEntity;
import com.wxy.zzarental.web.app.entity.GraphInfo;
import com.wxy.zzarental.web.app.entity.ViewAppointment;
import com.wxy.zzarental.web.app.mapper.ApartmentInfoMapper;
import com.wxy.zzarental.web.app.mapper.ViewAppointmentMapper;
import com.wxy.zzarental.web.app.service.ApartmentInfoService;
import com.wxy.zzarental.web.app.service.GraphInfoService;
import com.wxy.zzarental.web.app.service.ViewAppointmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.dto.ApartmentItemDTO;
import com.wxy.zzarental.web.app.service.dto.AppointmentDetailDTO;
import com.wxy.zzarental.web.app.service.dto.AppointmentItemDTO;
import com.wxy.zzarental.web.app.service.dto.GraphDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    @Resource
    private ViewAppointmentMapper viewAppointmentMapper;
    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;
    @Resource
    private ApartmentInfoService apartmentInfoService;
    @Resource
    private GraphInfoService graphInfoService;
    @Override
    public List<AppointmentItemDTO> listItem() {
        LambdaQueryWrapper<ViewAppointment> viewAppointmentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        viewAppointmentLambdaQueryWrapper.eq(ViewAppointment::getUserId, LoginUserHolder.getLoginUser().getUserId());
        viewAppointmentLambdaQueryWrapper.eq(ViewAppointment::getAppointmentStatus,1);
        List<ViewAppointment> viewAppointments = viewAppointmentMapper.selectList(viewAppointmentLambdaQueryWrapper);

        //公寓名称列表
        List<Long> apartmentIds = viewAppointments.stream().map(ViewAppointment::getApartmentId).toList();
        if (CollUtil.isEmpty(apartmentIds)){
            return Collections.emptyList();
        }
        List<ApartmentInfo> apartmentInfos = apartmentInfoMapper.selectBatchIds(apartmentIds);

        Map<Long, String> apartmentNameMap = apartmentInfos.stream().collect(Collectors.toMap(BaseEntity::getId, ApartmentInfo::getName, (key1, key2) -> key1));
        //图片
        Map<Long, List<GraphInfo>> graphMap = graphInfoService.mapByItemIds(1, apartmentIds);
        return viewAppointments.stream().map(
                viewAppointment -> {

                    AppointmentItemDTO appointmentItemVo = new AppointmentItemDTO();
                    BeanUtil.copyProperties(viewAppointment,appointmentItemVo);
                    //公寓id
                    Long apartmentId = viewAppointment.getApartmentId();

                    if (apartmentId != null) {
                        //公寓名称
                        appointmentItemVo.setApartmentName(apartmentNameMap.get(apartmentId));
                        //公寓图片
                        List<GraphInfo> graphInfos = graphMap.getOrDefault(apartmentId,new ArrayList<>());
                        List<GraphDTO> graphVos = graphInfos.stream().map(
                                graphInfo -> {
                                    return new GraphDTO(graphInfo.getName(), graphInfo.getUrl());
                                }
                        ).toList();
                        appointmentItemVo.setGraphVoList(graphVos);
                    }
                    return appointmentItemVo;

                }
        ).toList();

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateViewAppointment(ViewAppointment viewAppointment) {
        // 校验公寓是否真的存在
        Long apartmentId = viewAppointment.getApartmentId();
        if(apartmentId == null){
            throw  new ZZAException(ResultCodeEnum.APARTMENTID_ERROR);
        }
        LambdaQueryWrapper<ApartmentInfo> apartmentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentInfoLambdaQueryWrapper.eq(BaseEntity::getId,apartmentId);
        long count = apartmentInfoService.count(apartmentInfoLambdaQueryWrapper);
        if (count == 0){
            throw  new ZZAException(ResultCodeEnum.APARTMENTID_ERROR);
        }

        // 保存或更新
        if (viewAppointment.getId() != null){
            viewAppointmentMapper.updateById(viewAppointment);
        }
        viewAppointmentMapper.insert(viewAppointment);

    }

    @Override
    public AppointmentDetailDTO getDetailById(Long id,Long userId) {
        ViewAppointment viewAppointment = viewAppointmentMapper.selectById(id);
        AppointmentDetailDTO appointmentDetailVo = new AppointmentDetailDTO();
        if(viewAppointment == null){
            throw new ZZAException(ResultCodeEnum.VIEW_APPOINTMENT_NOT_EXIST);
        }
        // 判断查询出来的记录的userId是不是当oK
        if (viewAppointment.getUserId() != userId){
            throw new ZZAException(ResultCodeEnum.VIEW_APPOINTMENT_ERROR);
        }

        BeanUtil.copyProperties(viewAppointment,appointmentDetailVo);
        //公寓基本信息
        ApartmentItemDTO apartmentItemVo = apartmentInfoService.getInfoById(viewAppointment.getApartmentId());
//        BeanUtil.copyProperties(apartmentItemVo,appointmentDetailVo);
        appointmentDetailVo.setApartmentItemVo(apartmentItemVo);
        return appointmentDetailVo;
    }
}




