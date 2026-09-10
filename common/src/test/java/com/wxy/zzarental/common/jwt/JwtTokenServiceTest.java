package com.wxy.zzarental.common.jwt;

import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.login.AuthenticationInterceptor;
import com.wxy.zzarental.common.login.LoginUserHolder;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    // Synthetic fixture, never an application or deployment credential.
    private static final String TEST_SECRET = "test-only-signing-key-never-use-in-production";

    @AfterEach
    void clearLoginContext() {
        LoginUserHolder.clear();
    }

    @Test
    void tokenKeepsOriginalAlgorithmClaimsAndTwentyFourHourLifetime() {
        JwtTokenService service = serviceWith(TEST_SECRET);
        long before = System.currentTimeMillis();
        String token = service.createToken(7L, "test-user");
        long after = System.currentTimeMillis();

        var parsed = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseClaimsJws(token);
        Claims claims = parsed.getBody();
        assertEquals("HS256", parsed.getHeader().getAlgorithm());
        assertEquals("User_Login", claims.getSubject());
        assertEquals(7L, claims.get("userId", Long.class));
        assertEquals("test-user", claims.get("userName", String.class));
        long ttl = Duration.ofHours(24).toMillis();
        assertTrue(claims.getExpiration().getTime() >= before + ttl - 1000);
        assertTrue(claims.getExpiration().getTime() <= after + ttl);
    }

    @Test
    void parsingKeepsOriginalMissingInvalidAndExpiredErrorCodes() {
        JwtTokenService service = serviceWith(TEST_SECRET);
        assertEquals(ResultCodeEnum.TOKEN_NOT_EXIST.getCode(),
                assertThrows(ZZAException.class, () -> service.parseToken(" ")).getCode());
        assertEquals(ResultCodeEnum.TOKEN_INVALID.getCode(),
                assertThrows(ZZAException.class, () -> service.parseToken("invalid-token")).getCode());

        String expiredToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
        assertEquals(ResultCodeEnum.TOKEN_EXPIRED.getCode(),
                assertThrows(ZZAException.class, () -> service.parseToken(expiredToken)).getCode());
    }

    @Test
    void signingConfigurationIsIndependentForEachApplicationInstance() {
        String token = serviceWith(TEST_SECRET).createToken(7L, "test-user");
        assertEquals("test-user", serviceWith(TEST_SECRET).parseToken(token).get("userName"));
        JwtTokenService otherApplication = serviceWith("another-test-only-key-never-use-in-production");
        assertEquals(ResultCodeEnum.TOKEN_INVALID.getCode(),
                assertThrows(ZZAException.class, () -> otherApplication.parseToken(token)).getCode());
    }

    @Test
    void sharedAuthenticationKeepsHeaderAndClearsContextAfterCompletion() {
        JwtTokenService service = serviceWith(TEST_SECRET);
        AuthenticationInterceptor interceptor = new AuthenticationInterceptor(service);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("access-token", service.createToken(7L, "test-user"));

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(7L, LoginUserHolder.getLoginUser().getUserId());
        assertEquals("test-user", LoginUserHolder.getLoginUser().getUserName());
        interceptor.afterCompletion(request, response, new Object(), new RuntimeException("test failure"));
        assertNull(LoginUserHolder.getLoginUser());
    }

    private JwtTokenService serviceWith(String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        return new JwtTokenService(properties);
    }
}
