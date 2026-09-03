package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.model.entity.BaseEntity;
import com.wxy.zzarental.model.entity.PaymentType;
import com.wxy.zzarental.model.entity.RoomPaymentType;
import com.wxy.zzarental.web.app.mapper.PaymentTypeMapper;
import com.wxy.zzarental.web.app.service.PaymentTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxy.zzarental.web.app.service.RoomPaymentTypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author liubo
* @description 针对表【payment_type(支付方式表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class PaymentTypeServiceImpl extends ServiceImpl<PaymentTypeMapper, PaymentType>
    implements PaymentTypeService{
    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Override
    public List<PaymentType> getPaymentTypeByRoomId(Long id) {
        //先拿到关联表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getId,id);
        List<RoomPaymentType> roomList = roomPaymentTypeService.list(roomPaymentTypeLambdaQueryWrapper);
        //拿到关联表中的paymentid
        List<Long> paymentIdList = roomList.stream().map(RoomPaymentType::getPaymentTypeId).distinct().collect(Collectors.toList());
        //判断该房间有没有支付方式
        if(CollUtil.isEmpty(paymentIdList)){
            return Collections.emptyList();
        }
        //通过查id在不在paymentidlist里面判断

        LambdaQueryWrapper<PaymentType> paymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        paymentTypeLambdaQueryWrapper.in(BaseEntity::getId,paymentIdList);

//        return paymentTypeMapper.selectList(paymentTypeLambdaQueryWrapper);
        // paymentTypeService.list()是通过对象.方法来调用PaymentTypeService类里面的list方法
        // 我们本来就是PaymentTypeService类，所以相当于调用自己的list方法
        return list(paymentTypeLambdaQueryWrapper);
    }
}




