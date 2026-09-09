package com.wxy.zzarental.web.app.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wxy
 * @description 基础的mq消息体,所有业务消息体都继承自这个类
 * @date 2026/09/07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseMQMsg {
    /**
     * 唯一id,防止重复消费
     */
    private String uuid;
}
