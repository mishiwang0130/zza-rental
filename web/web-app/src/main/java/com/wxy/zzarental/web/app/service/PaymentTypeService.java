package com.wxy.zzarental.web.app.service;

import com.wxy.zzarental.model.entity.PaymentType;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【payment_type(支付方式表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface PaymentTypeService extends IService<PaymentType> {
    /**
     * 按房间id获取付款类型
     *
     * @param id ID
     * @return {@code List<PaymentType> }
     * @author wxy
     * @date 2026/09/03
     */
    List<PaymentType> getPaymentTypeByRoomId(Long id);

}
