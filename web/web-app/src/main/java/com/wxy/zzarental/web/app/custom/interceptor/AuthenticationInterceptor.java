package com.wxy.zzarental.web.app.custom.interceptor;

import com.wxy.zzarental.common.login.LoginUser;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    // olloma(部署在自己的服务器上，成本在十万以上)


    // chatgpt -> openai协议
    // claude -> anthropic协议
    // 其他协议 -> 每一家都不一样(需要引入厂商自己的sdk，无法使用ai框架),其他ai基本都会适配openai协议

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("access-token");
        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String userName = claims.get("userName", String.class);
        LoginUserHolder.setThreadLocal(new LoginUser(userId,userName));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginUserHolder.clear();
    }
}

