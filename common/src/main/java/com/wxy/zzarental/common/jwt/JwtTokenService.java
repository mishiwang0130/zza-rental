package com.wxy.zzarental.common.jwt;

import cn.hutool.core.util.StrUtil;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Shared implementation; each application supplies its own signing configuration. */
public class JwtTokenService {

    private static final long TOKEN_TTL_MILLIS = 86_400_000L;

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    public JwtTokenService(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
    }

    public String createToken(Long id, String username) {
        return Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TTL_MILLIS))
                .setSubject("User_Login")
                .claim("userId", id)
                .claim("userName", username)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            throw new ZZAException(ResultCodeEnum.TOKEN_NOT_EXIST);
        }
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new ZZAException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new ZZAException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
}
