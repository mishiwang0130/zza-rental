package com.wxy.aicustomer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 公寓智能客服服务启动类。
 *
 * <p>服务面向匿名访客：不登录、不注册，只通过 visitorId / conversationId 区分会话。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AiCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCustomerApplication.class, args);
    }
}
