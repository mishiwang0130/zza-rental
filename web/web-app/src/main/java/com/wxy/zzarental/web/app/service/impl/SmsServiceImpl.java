package com.wxy.zzarental.web.app.service.impl;

import com.aliyun.credentials.models.Config;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.tea.TeaException;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.web.app.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendCode(String phone, String code, String expireMinute) {
        // {"code":"123456","min":"5"}
        String templateParam = "{\"code\":\"" + code + "\",\"min\":\"" + expireMinute + "\"}";
        com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                .setSignName("恒创联众")
                .setTemplateCode("100001")
                .setPhoneNumber(phone)
                .setTemplateParam(templateParam);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            com.aliyun.dypnsapi20170525.Client client = createClient();
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse resp = client.sendSmsVerifyCodeWithOptions(sendSmsVerifyCodeRequest, runtime);
            if (resp.getStatusCode() != 200) {
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
            SendSmsVerifyCodeResponseBody respBody = resp.getBody();
            if (respBody == null) {
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
            if(!respBody.getCode().equals("OK")){
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
            if (!respBody.getSuccess()){
                throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
            }
        } catch (TeaException error) {
            log.error("TeaException: {}", error.getMessage(), error);
            throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
        } catch (Exception _error) {
            log.error("Exception: {}", _error.getMessage(), _error);
            throw new ZZAException(ResultCodeEnum.APP_SEND_SMS_ERROR);
        }
    }

    /**
     * <b>description</b> :
     * <p>使用凭据初始化账号Client</p>
     * @return Client
     *
     * @throws Exception
     */
    private com.aliyun.dypnsapi20170525.Client createClient() throws Exception {
        Config credentialConfig = new Config();
        credentialConfig.setType("access_key");
        // 必填参数，此处以从环境变量中获取AccessKey ID为例
        credentialConfig.setAccessKeyId("LTAI5tANcrbmnrwiPP4YZ7LP");
        // 必填参数，此处以从环境变量中获取AccessKey Secret为例
        credentialConfig.setAccessKeySecret("NDVVE3IBEejGQBzf1qtEhjrY8e4T2u");
        com.aliyun.credentials.Client credentialClient = new com.aliyun.credentials.Client(credentialConfig);

        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setCredential(credentialClient);
        // Endpoint 请参考 https://api.aliyun.com/product/Dypnsapi
        config.endpoint = "dypnsapi.aliyuncs.com";
        return new com.aliyun.dypnsapi20170525.Client(config);
    }
}
