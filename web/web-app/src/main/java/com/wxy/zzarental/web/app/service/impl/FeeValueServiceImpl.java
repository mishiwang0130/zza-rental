package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.model.entity.ApartmentFeeValue;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.FeeKey;
import com.wxy.zzarental.model.entity.FeeValue;
import com.wxy.zzarental.web.app.mapper.ApartmentFeeValueMapper;
import com.wxy.zzarental.web.app.mapper.FeeKeyMapper;
import com.wxy.zzarental.web.app.mapper.RoomInfoMapper;
import com.wxy.zzarental.web.app.service.FeeValueService;
import com.wxy.zzarental.web.app.mapper.FeeValueMapper;
import com.wxy.zzarental.web.app.vo.fee.FeeValueVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【fee_value(杂项费用值表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue>
    implements FeeValueService{
    @Resource
    private ApartmentFeeValueMapper apartmentFeeValueMapper;
    @Resource
    private FeeValueMapper feeValueMapper;
    @Resource
    private FeeKeyMapper feeKeyMapper;

    @Override
    public List<FeeValueVo> listByApartmentId(Long id) {

        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueMapper.selectList(apartmentFeeValueLambdaQueryWrapper);
        List<Long> feevalueIds = apartmentFeeValues.stream().map(ApartmentFeeValue::getFeeValueId).toList();
        List<FeeValue> feeValues = feeValueMapper.selectBatchIds(feevalueIds);
        List<Long> feekeyIds = feeValues.stream().map(FeeValue::getFeeKeyId).distinct().toList();
        Map<Long, String> feeNameMap = feeKeyMapper.selectBatchIds(feekeyIds).stream().collect(Collectors.toMap(BaseEntity::getId, FeeKey::getName,(key1, key2)->key1));
        return feeValues.stream().map(
                feeValue -> {
                    FeeValueVo feeValueVo = new FeeValueVo();
                    BeanUtil.copyProperties(feeValue, feeValueVo);
                    feeValueVo.setFeeKeyName(feeNameMap.get(feeValue.getFeeKeyId()));
                    return feeValueVo;
                }
        ).toList();
    }
}




