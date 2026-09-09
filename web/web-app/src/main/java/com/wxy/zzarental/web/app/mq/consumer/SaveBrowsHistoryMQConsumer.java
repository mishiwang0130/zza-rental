package com.wxy.zzarental.web.app.mq.consumer;

import com.wxy.zzarental.common.constant.MQConstant;
import com.wxy.zzarental.common.util.RedisUtil;
import com.wxy.zzarental.web.app.mq.message.SaveBrowsHistoryMQMsg;
import com.wxy.zzarental.web.app.service.BrowsingHistoryService;
import com.wxy.zzarental.web.app.service.SmsService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(topic = MQConstant.SAVE_BROWSING_HISTORY_TOPIC ,consumerGroup = MQConstant.ZZA_GROUP)
public class SaveBrowsHistoryMQConsumer implements RocketMQListener<SaveBrowsHistoryMQMsg> {

    @Resource
    private BrowsingHistoryService browsingHistoryService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public void onMessage(SaveBrowsHistoryMQMsg saveBrowsHistoryMQMsg) {
        String uuid = saveBrowsHistoryMQMsg.getUuid();
        // 通过uuid查询消息执行失败表
//        FildMq fildMq = fildMqService.getByMessageId(uuid);
//        if (fildMq.getSendCount() >= 3){
//            // 发短信通知负责人
//            smsService.sendSms("13800000000", "保存浏览记录失败3次");
//            return;
//        }

        if (redisUtil.hasKey(uuid)){
            return;
        }

        redisUtil.set(uuid, uuid, 60 *60, TimeUnit.SECONDS);
        try {

            browsingHistoryService.saveHistory(saveBrowsHistoryMQMsg.getUserId(), saveBrowsHistoryMQMsg.getRoomId());
        } catch (Exception e) {
            // 保存到消息执行失败表,最大重试三次
            // 保存这个消息体的json字符串、corn、uuid，方便定时任务重试，或者审计查看消息体，
//            String json = saveBrowsHistoryMQMsg.toJson();
            // fildMqService.insert(new FildMq(json, 60*60*2, uuid, MQConstant.SAVE_BROWSING_HISTORY_TOPIC));
        }

    }

    public void job (){
        // List list = fildMqService.list();
//        for (FildMq fildMq : list) {
//            String json = fildMq.getJson();
//            SaveBrowsHistoryMQMsg saveBrowsHistoryMQMsg = JSON.parseObject(json, SaveBrowsHistoryMQMsg.class);
//            // 重试
//            rocketMQTemplate.convertAndSend(MQConstant.SAVE_BROWSING_HISTORY_TOPIC,saveBrowsHistoryMQMsg);
//            fildMq.setSendCount(fildMq.getSendCount() + 1);
//        }
//        fildMqService.updateBatchById(list);


    }
}
