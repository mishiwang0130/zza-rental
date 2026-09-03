package com.wxy.zzarental.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.LabelInfo;
import com.wxy.zzarental.model.entity.RoomLabel;
import com.wxy.zzarental.web.app.mapper.RoomLabelMapper;
import com.wxy.zzarental.web.app.service.LabelInfoService;
import com.wxy.zzarental.web.app.mapper.LabelInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【label_info(标签信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo>
    implements LabelInfoService{
    @Resource
    private LabelInfoMapper labelInfoMapper;
    @Resource
    private RoomLabelMapper roomLabelMapper;

    @Override
    public List<LabelInfo> listByRoomId(Long id) {
        LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelLambdaQueryWrapper.eq(RoomLabel::getRoomId, id);
        List<RoomLabel> roomLabels = roomLabelMapper.selectList(roomLabelLambdaQueryWrapper);
        List<Long> labelIds = roomLabels.stream().map(RoomLabel::getLabelId).toList();

        return labelInfoMapper.selectBatchIds(labelIds);
    }
}




