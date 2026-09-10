package com.wxy.zzarental.web.app.infrastructure.messaging;

import com.wxy.zzarental.common.constant.MQConstant;
import com.wxy.zzarental.web.app.mq.message.SaveBrowsHistoryMQMsg;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Component
public class RocketMqBrowsingHistoryMessagePublisher implements BrowsingHistoryMessagePublisher {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqBrowsingHistoryMessagePublisher(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void publish(SaveBrowsHistoryMQMsg message) {
        rocketMQTemplate.convertAndSend(MQConstant.SAVE_BROWSING_HISTORY_TOPIC, message);
    }
}
