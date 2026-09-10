package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.model.entity.AttrValue;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.RoomAttrValue;
import com.wxy.zzarental.web.app.mapper.AttrKeyMapper;
import com.wxy.zzarental.web.app.mapper.RoomAttrValueMapper;
import com.wxy.zzarental.web.app.service.AttrValueService;
import com.wxy.zzarental.web.app.mapper.AttrValueMapper;
import com.wxy.zzarental.web.app.service.dto.AttrValueDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【attr_value(房间基本属性值表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue>
    implements AttrValueService{

    @Resource
    private RoomAttrValueMapper roomAttrValueMapper;
    @Resource
    private AttrValueMapper attrValueMapper;
    @Resource
    private AttrKeyMapper attrKeyMapper;

    @Override
    public List<AttrValueDTO> listByRoomId(Long roomId) {
        LambdaQueryWrapper<RoomAttrValue> attrValueVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attrValueVoLambdaQueryWrapper.eq(RoomAttrValue::getRoomId, roomId);
        List<RoomAttrValue> roomAttrValues = roomAttrValueMapper.selectList(attrValueVoLambdaQueryWrapper);
        if (CollUtil.isEmpty(roomAttrValues)) {
            return Collections.emptyList();
        }
        Set<Long> attrValueIds = roomAttrValues.stream().map(RoomAttrValue::getAttrValueId).collect(Collectors.toSet());
        List<AttrValue> attrValues = attrValueMapper.selectBatchIds(attrValueIds);
        List<Long> attrKeyIds = attrValues.stream().map(AttrValue::getAttrKeyId).distinct().toList();
        //通过attrKeyid去attr_key表查attrKeyName
        Map<Long, String> attrKeyMap = attrKeyMapper.selectBatchIds(attrKeyIds)
                .stream().collect(Collectors.toMap(BaseEntity::getId, AttrKey::getName, (key1, key2) -> key1));
        return attrValues.stream().map(
                attrValue -> {
                    AttrValueDTO attrValueVo = new AttrValueDTO();
                    BeanUtil.copyProperties(attrValue, attrValueVo);
                    attrValueVo.setAttrKeyName(attrKeyMap.get(attrValue.getAttrKeyId()));
                    return attrValueVo;
                }
        ).toList();
    }
}




