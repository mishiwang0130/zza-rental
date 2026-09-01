package com.wxy.zzarental.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @author wxy
 * @description jwt,生成token
 * @date 2026/09/01
 */
public class JwtUtil {

    private static SecretKey secretKey = Keys.hmacShaKeyFor("a99c28Ed65114571aa1e4aa9e2c8c813".getBytes());

    public static  String createToken(Long id,String username){
        String jwt = Jwts.builder().setExpiration(new Date(System.currentTimeMillis() + 86400000)).
                setSubject("User_Login")
                .claim("userId", id)
                .claim("userName", username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;

    }

    /**
     * 解析令牌
     *
     * @param token
     * @author wxy
     * @date 2026/09/01
     */
    public static void parseToken(String token){
        if(StrUtil.isBlank(token)){
            throw new ZZAException(ResultCodeEnum.TOKEN_NOT_EXIST);
        }


        try {
            // 解析token
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            jwtParser.parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new ZZAException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e){
            throw new ZZAException(ResultCodeEnum.TOKEN_INVALID);
        }


    }

//    public static void main(String[] args) {
//        JwtUtil.createToken(1L,"json");
//
//    }
}
