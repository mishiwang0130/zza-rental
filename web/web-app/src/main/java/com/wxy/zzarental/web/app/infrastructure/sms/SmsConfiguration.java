package com.wxy.zzarental.web.app.infrastructure.sms;

import com.aliyun.credentials.models.Config;
import com.aliyun.dypnsapi20170525.Client;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfiguration {

    @Bean
    public Client smsClient(SmsProperties properties) throws Exception {
        Config credentials = new Config();
        credentials.setType("access_key");
        credentials.setAccessKeyId(properties.getAccessKeyId());
        credentials.setAccessKeySecret(properties.getAccessKeySecret());

        com.aliyun.credentials.Client credentialClient = new com.aliyun.credentials.Client(credentials);
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setCredential(credentialClient);
        config.endpoint = properties.getEndpoint();
        return new Client(config);
    }
}
