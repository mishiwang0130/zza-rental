package com.wxy.zzarental.common.constant;

/**
 * @author wxy
 * @description mq的topic常数
 * @date 2026/09/07
 */
public interface MQConstant {
    /**
     * group名称
     */
    String ZZA_GROUP = "zza_group";

    /**
     * 前缀topic
     */
    String ZZA_TOPIC_PREFIX = "zza_";
    /**
     * 保存浏览历史的topic
     */
    String SAVE_BROWSING_HISTORY_TOPIC = ZZA_TOPIC_PREFIX + "save_browsing_history";
}
