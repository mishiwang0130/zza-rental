package com.wxy.zzarental.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.model.entity.AttrKey;
import com.wxy.zzarental.model.entity.AttrValue;
import com.wxy.zzarental.web.admin.mapper.AttrKeyMapper;
import com.wxy.zzarental.web.admin.mapper.AttrValueMapper;
import com.wxy.zzarental.web.admin.service.AttrKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeyVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class AttrKeyServiceImpl extends ServiceImpl<AttrKeyMapper, AttrKey>
    implements AttrKeyService{
    @Resource
    private AttrKeyMapper attrKeyMapper;

    @Resource
    private AttrValueMapper attrValueMapper;

    @Override
    public List<AttrKeyVo> listAttrInfo() {
        return attrKeyMapper.selectAttrInfo();
    }

    @Override
    public void removeAttrKeyById(Long attrKeyId) {
        attrKeyMapper.deleteById(attrKeyId);
        LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrValue::getAttrKeyId, attrKeyId);
        List<AttrValue> attrValueList = attrValueMapper.selectList(queryWrapper);
        if (attrValueList != null && !attrValueList.isEmpty()) {
            List<Long> attrValueIds = attrValueList.stream().map(AttrValue::getId).collect(Collectors.toList());
            attrValueMapper.deleteBatchIds(attrValueIds);
        }
    }
}




