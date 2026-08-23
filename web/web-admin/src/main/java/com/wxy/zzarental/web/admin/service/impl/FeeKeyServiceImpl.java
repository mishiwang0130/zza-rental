package com.wxy.zzarental.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.model.entity.FeeKey;
import com.wxy.zzarental.model.entity.FeeValue;
import com.wxy.zzarental.web.admin.mapper.FeeKeyMapper;
import com.wxy.zzarental.web.admin.mapper.FeeValueMapper;
import com.wxy.zzarental.web.admin.service.FeeKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.admin.vo.fee.FeeKeyVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author liubo
* @description 针对表【fee_key(杂项费用名称表)】的数据库操作Service实现
* @createDate 2023-07-24 15:48:00
*/
@Service
public class FeeKeyServiceImpl extends ServiceImpl<FeeKeyMapper, FeeKey>
    implements FeeKeyService{

    @Resource
    private FeeKeyMapper feeKeyMapper;
    @Resource
    private FeeValueMapper feeValueMapper;
    @Override
    public List<FeeKeyVo> feeInfoList() {
        return feeKeyMapper.selectList();
    }

    @Override
    public void deleteFeeKeyById(Long feeKeyId) {
        feeKeyMapper.deleteById(feeKeyId);
        // 删除关联的杂费值
        LambdaQueryWrapper<FeeValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeeValue::getFeeKeyId, feeKeyId);
        List<FeeValue> feeValueList = feeValueMapper.selectList(queryWrapper);
        feeValueMapper.deleteBatchIds(feeValueList);
    }
}




