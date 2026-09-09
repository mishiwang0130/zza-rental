package com.wxy.zzarental.web.app.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wxy
 * @description 保存浏览记录的mq消息体,规范要以MQMsg结尾
 * @date 2026/09/07
 */
@Data
@NoArgsConstructor
public class SaveBrowsHistoryMQMsg extends BaseMQMsg {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 房间号
     */
    private Long roomId;

    public SaveBrowsHistoryMQMsg(String uuid, Long userId, Long roomId) {
        super(uuid);
        this.userId = userId;
        this.roomId = roomId;
    }
}
