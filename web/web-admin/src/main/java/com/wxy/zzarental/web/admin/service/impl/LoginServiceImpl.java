package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.wf.captcha.SpecCaptcha;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.web.admin.service.LoginService;
import com.wxy.zzarental.common.util.RedisKeyUtil;
import com.wxy.zzarental.web.admin.vo.login.CaptchaVo;
import com.wxy.zzarental.web.admin.vo.login.LoginVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String code = specCaptcha.text().toLowerCase();
        String key = UUID.randomUUID().toString(true);
        String redisKey = RedisKeyUtil.getCaptcha(key);
        stringRedisTemplate.opsForValue().set(redisKey,code,60, TimeUnit.SECONDS);
        return new CaptchaVo(specCaptcha.toBase64(),key);
    }

    @Override
    public String login(LoginVo loginVo) {

        //根据key去redis查code，为空说明过期
        String key = loginVo.getCaptchaKey();
        String redisKey = RedisKeyUtil.getCaptcha(key);
        String code = stringRedisTemplate.opsForValue().get(redisKey);
        if(StrUtil.isBlank(code)){
            throw new ZZAException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        //验证码不对抛异常
        if (!code.equals(loginVo.getCaptchaCode())){
            throw new ZZAException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }
        //名字查数据库，不对抛
        //查用户状态，禁用就抛
        //根据名字判断密码对不对，不对抛
        return "";
    }
}
