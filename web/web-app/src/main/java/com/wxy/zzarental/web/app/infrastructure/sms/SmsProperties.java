package com.wxy.zzarental.web.app.infrastructure.sms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "integration.sms")
public class SmsProperties {

    @NotBlank
    private String accessKeyId;
    @NotBlank
    private String accessKeySecret;
    @NotBlank
    private String endpoint;
    @NotBlank
    private String signName;
    @NotBlank
    private String templateCode;

    // Null keeps the SDK's existing timeout defaults.
    @Min(1)
    private Integer connectTimeoutMs;
    @Min(1)
    private Integer readTimeoutMs;
}
