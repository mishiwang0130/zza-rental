package com.wxy.zzarental.web.app.infrastructure.messaging;

import com.wxy.zzarental.web.app.mq.message.SaveBrowsHistoryMQMsg;

public interface BrowsingHistoryMessagePublisher {

    void publish(SaveBrowsHistoryMQMsg message);
}
