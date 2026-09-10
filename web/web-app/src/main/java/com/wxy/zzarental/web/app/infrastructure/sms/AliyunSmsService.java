package com.wxy.zzarental.web.app.infrastructure.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.web.app.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AliyunSmsService implements SmsService {

    private final Client client;
    private final SmsProperties properties;

    public AliyunSmsService(Client client, SmsProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void sendCode(String phone, String code, String expireMinute) {
        String templateParam = "{\"code\":\"" + code + "\",\"min\":\"" + expireMinute + "\"}";
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setSignName(properties.getSignName())
                .setTemplateCode(properties.getTemplateCode())
                .setPhoneNumber(phone)
                .setTemplateParam(templateParam);
        RuntimeOptions runtime = new RuntimeOptions();
        if (properties.getConnectTimeoutMs() != null) {
            runtime.setConnectTimeout(properties.getConnectTimeoutMs());
        }
        if (properties.getReadTimeoutMs() != null) {
            runtime.setReadTimeout(properties.getReadTimeoutMs());
        }
        try {
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);
            if (response.getStatusCode() != 200) {
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
            SendSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !body.getCode().equals("OK") || !body.getSuccess()) {
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
        } catch (TeaException e) {
            log.error("短信服务调用失败", e);
            throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
        } catch (Exception e) {
            log.error("短信发送失败", e);
            throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
        }
    }
}
