package com.xiangyun.account.service;

import com.xiangyun.account.dto.AccountDTO;
import com.xiangyun.account.entity.UserAccount;
import com.xiangyun.account.mapper.UserAccountMapper;
import com.xiangyun.common.context.RlsContext;
import com.xiangyun.common.exception.BizException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserAccountMapper userAccountMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret:xiangyun-zhifang-jwt-secret-key-min-256-bits-change-in-production!!}")
    private String jwtSecret;

    @Value("${jwt.expiration:7200}")
    private long jwtExpiration; // 秒

    @Value("${jwt.refresh-expiration:604800}")
    private long refreshExpiration; // 7天

    /**
     * 用戶登入
     */
    public AccountDTO.LoginResponse login(AccountDTO.LoginRequest request) {
        UserAccount user = userAccountMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new BizException(BizException.ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != 1) {
            throw new BizException(BizException.ErrorCode.ACCOUNT_DISABLED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(BizException.ErrorCode.PASSWORD_ERROR);
        }

        // 更新最後登入時間
        user.setLastLoginAt(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        userAccountMapper.updateById(user);

        return buildLoginResponse(user);
    }

    /**
     * 用戶註冊
     */
    @Transactional
    public AccountDTO.LoginResponse register(AccountDTO.RegisterRequest request) {
        // 檢查用戶名是否重複
        userAccountMapper.findByUsername(request.getUsername())
                .ifPresent(u -> { throw new BizException(409, "用戶名已存在"); });

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("patient");
        user.setStatus(1);
        userAccountMapper.insert(user);

        log.info("New user registered: username={}, id={}", user.getUsername(), user.getId());
        return buildLoginResponse(user);
    }

    /**
     * 刷新 Token
     */
    public AccountDTO.LoginResponse refresh(String refreshToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String tokenType = claims.get("type", String.class);

            if (!"refresh".equals(tokenType)) {
                throw new BizException(BizException.ErrorCode.TOKEN_INVALID);
            }

            // 檢查 Redis 中的 Refresh Token 是否有效
            String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                throw new BizException(BizException.ErrorCode.TOKEN_INVALID);
            }

            UserAccount user = userAccountMapper.selectById(userId);
            if (user == null || user.getStatus() != 1) {
                throw new BizException(BizException.ErrorCode.ACCOUNT_DISABLED);
            }

            return buildLoginResponse(user);

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(BizException.ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 登出（將 Token 加入黑名單）
     */
    public void logout(String accessToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();

            if (remainingMs > 0) {
                // 將 Token 加入 Redis 黑名單，設置為剩餘有效時間
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken, "1",
                        remainingMs, TimeUnit.MILLISECONDS);
            }

            Long userId = claims.get("userId", Long.class);
            redisTemplate.delete("refresh_token:" + userId);

        } catch (Exception e) {
            log.warn("Logout token parse failed: {}", e.getMessage());
        }
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private AccountDTO.LoginResponse buildLoginResponse(UserAccount user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        // Access Token (2小時)
        String accessToken = Jwts.builder()
                .claim("userId", user.getId())
                .claim("userName", user.getRealName())
                .claim("clinicId", user.getClinicId())
                .claim("role", user.getRole())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtExpiration)))
                .signWith(key)
                .compact();

        // Refresh Token (7天)
        String refreshToken = Jwts.builder()
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpiration)))
                .signWith(key)
                .compact();

        // 存儲 Refresh Token 到 Redis
        redisTemplate.opsForValue().set(
                "refresh_token:" + user.getId(),
                refreshToken,
                refreshExpiration, TimeUnit.SECONDS);

        return AccountDTO.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userInfo(AccountDTO.LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole())
                        .clinicId(user.getClinicId())
                        .build())
                .build();
    }
}
