package com.wxy.zzarental.web.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.common.util.JwtUtil;
import com.wxy.zzarental.model.entity.SystemUser;
import com.wxy.zzarental.model.enums.BaseEnum;
import com.wxy.zzarental.model.enums.BaseStatus;
import com.wxy.zzarental.web.admin.mapper.SystemUserMapper;
import com.wxy.zzarental.web.admin.service.LoginService;
import com.wxy.zzarental.common.util.RedisKeyUtil;
import com.wxy.zzarental.web.admin.vo.login.CaptchaRespVO;
import com.wxy.zzarental.web.admin.vo.login.LoginReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserInfoRespVO;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Resource
    private SystemUserMapper systemUserMapper;

    @Override
    public CaptchaRespVO getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String code = specCaptcha.text().toLowerCase();
        String key = UUID.randomUUID().toString(true);
        String redisKey = RedisKeyUtil.getCaptcha(key);
        // 将code保存到redis中，并设置60*5秒的过期时间
        stringRedisTemplate.opsForValue().set(redisKey,code,60*5, TimeUnit.SECONDS);
        return new CaptchaRespVO(specCaptcha.toBase64(),key);
    }
    @Transactional(rollbackFor = ZZAException.class)
    @Override
    public String login(LoginReqVO loginVo) {

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
        LambdaQueryWrapper<SystemUser> systemUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        systemUserLambdaQueryWrapper.eq(SystemUser::getUsername,loginVo.getUsername());
        // selectOne是从数据库中查询一条，如果通过条件查出了多条就会报错
        // 虽然注册会让用户名不能相同，但是由于数据库可能有脏数据或者其他原因，所以需要防御性编程
        // 为了防止真的获取到多条，所以条件需要显式只获取第一条，这样不管有多少条，selectOne都只会拿到一条，永远不会报错
        // last()是可以在sql的最后面拼接一个手写的sql，一般的用途就是配合selectOne写limit 1，这个limit 1 就是真正的sql
        //我知道，limit是分页那个呢，第一页第一条，是的，加上这个就selectOne都只会拿到一条
        systemUserLambdaQueryWrapper.last("limit 1");
        SystemUser systemUser = systemUserMapper.selectOne(systemUserLambdaQueryWrapper);
        if (systemUser == null){
            throw new ZZAException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }
        //查用户状态，禁用就抛
        if (systemUser.getStatus().equals(BaseStatus.DISABLE)){
            throw new ZZAException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        //根据名字判断密码对不对，不对抛
        // 真实的场景中，数据库中的密码都是加密过的，并且用的不是md5的加密方式，而且其他更安全的加密算法
        // 真实的场景会有一个加密后的密码和一个与这个密码匹配的盐值，会将用户传进来的密码+数据库盐值的用同样的算法去加密
        // 然后用这个算法的比较方法（通常是恒定时间比较函数），去将数据库中的密码和加密后的用户传入的密码进行比较
        // 这里使用md5是非常不推荐的，会有严重的安全问题
        if (! systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){
            throw new ZZAException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        return JwtUtil.createToken(systemUser.getId(),systemUser.getUsername());
    }

    @Override
    public SystemUserInfoRespVO getLoginUserInfoById(Long userId) {
        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoRespVO systemUserInfoVo = new SystemUserInfoRespVO();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}
