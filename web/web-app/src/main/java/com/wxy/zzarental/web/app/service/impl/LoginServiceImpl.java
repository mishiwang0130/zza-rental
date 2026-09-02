package com.wxy.zzarental.web.app.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxy.zzarental.common.constant.RedisKeyConstant;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.common.util.JwtUtil;
import com.wxy.zzarental.model.entity.UserInfo;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.app.mapper.UserInfoMapper;
import com.wxy.zzarental.web.app.service.LoginService;
import com.wxy.zzarental.web.app.vo.user.LoginVo;
import com.wxy.zzarental.web.app.vo.user.UserInfoVo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String login(LoginVo loginVo) {

        String key = RedisKeyConstant.APP_LOGIN_KEY + loginVo.getPhone();
        String code = redisTemplate.opsForValue().get(key).toString();
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
        return JwtUtil.createToken(userInfo.getId(),userInfo.getPhone());
    }
}
