package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.constant.RedisKeyConstant;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.common.jwt.JwtTokenService;
import com.wxy.zzarental.common.util.RedisKeyUtil;
import com.wxy.zzarental.model.entity.UserInfo;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.app.mapper.UserInfoMapper;
import com.wxy.zzarental.web.app.service.LoginService;
import com.wxy.zzarental.web.app.service.SmsService;
import com.wxy.zzarental.web.app.service.command.LoginCommand;
import com.wxy.zzarental.web.app.service.dto.UserInfoDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private SmsService smsService;
    @Resource
    private JwtTokenService jwtTokenService;


    @Override
    public String login(LoginCommand loginVo) {

        String key = RedisKeyUtil.getPhoneCaptcha(loginVo.getPhone());
        String code = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(code)){
            throw new ZZAException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode())){
            throw new ZZAException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);

        //用户不存在就注册，登录的话要判断状态是否禁用
        if (userInfo == null){
            //注册
            userInfo  = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户"+loginVo.getPhone());
            userInfoMapper.insert(userInfo);
        }else {
            if (userInfo.getStatus().equals(BaseStatus.DISABLE)){
                throw new ZZAException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }
        return jwtTokenService.createToken(userInfo.getId(),userInfo.getPhone());
    }


    @Override
    public UserInfoDTO getLoginUserById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return new UserInfoDTO(userInfo.getNickname(),userInfo.getAvatarUrl());
    }

    @Override
    public void sendSmsCode(String phone) {
        //拿到随机code
        String code = RandomUtil.randomNumbers(6);
        //sms发送短信
        smsService.sendCode(phone,code,"5");
        String key = RedisKeyUtil.getPhoneCaptcha(phone);
        stringRedisTemplate.opsForValue().set(key,code,60*5, TimeUnit.SECONDS);
    }
}
