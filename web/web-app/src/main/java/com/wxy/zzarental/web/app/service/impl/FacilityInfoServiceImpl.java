package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.entity.FacilityInfo;
import com.wxy.zzarental.web.app.entity.RoomFacility;
import com.wxy.zzarental.web.app.mapper.RoomFacilityMapper;
import com.wxy.zzarental.web.app.service.FacilityInfoService;
import com.wxy.zzarental.web.app.mapper.FacilityInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【facility_info(配套信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo>
    implements FacilityInfoService{

    @Resource
    private RoomFacilityMapper roomFacilityMapper;

    @Override
    public List<FacilityInfo> listByRoomId(Long roomId) {
        LambdaQueryWrapper<RoomFacility> roomFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityLambdaQueryWrapper.eq(RoomFacility::getRoomId, roomId);
        List<RoomFacility> roomFacilities = roomFacilityMapper.selectList(roomFacilityLambdaQueryWrapper);
        if (CollUtil.isEmpty(roomFacilities)) {
            return Collections.emptyList();
        }
        List<Long> facilityIds = roomFacilities.stream().map(RoomFacility::getFacilityId).collect(Collectors.toList());
        return listByIds(facilityIds);
    }
}




