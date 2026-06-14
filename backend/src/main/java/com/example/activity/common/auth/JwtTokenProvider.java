package com.example.activity.common.auth;

import com.example.activity.common.config.JwtProperties;
import com.example.activity.common.enums.RoleTypeEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE_TYPE = "roleType";
    private static final String CLAIM_PROVINCE = "provinceName";
    private static final String CLAIM_CITY = "cityName";
    private static final String CLAIM_DISTRICT = "districtName";
    private static final String CLAIM_SCHOOL = "schoolName";

    private final JwtProperties jwtProperties;

    public String generateToken(AuthUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE_TYPE, user.getRoleType().getValue())
                .claim(CLAIM_PROVINCE, nullToEmpty(user.getProvinceName()))
                .claim(CLAIM_CITY, nullToEmpty(user.getCityName()))
                .claim(CLAIM_DISTRICT, nullToEmpty(user.getDistrictName()))
                .claim(CLAIM_SCHOOL, nullToEmpty(user.getSchoolName()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    public AuthUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return AuthUser.of(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_USERNAME, String.class),
                resolveRoleType(claims),
                emptyToNull(claims.get(CLAIM_PROVINCE, String.class)),
                emptyToNull(claims.get(CLAIM_CITY, String.class)),
                emptyToNull(claims.get(CLAIM_DISTRICT, String.class)),
                emptyToNull(claims.get(CLAIM_SCHOOL, String.class))
        );
    }

    /** @deprecated 兼容旧调用，请使用 {@link #generateToken(AuthUser)} */
    @Deprecated
    public String generateToken(LoginUser user) {
        return generateToken(AuthUser.of(
                user.getId(),
                user.getUsername(),
                RoleTypeEnum.fromLegacyUserRole(user.getRole()),
                null, null, null, null
        ));
    }

    /** @deprecated 兼容旧调用，请使用 {@link #parseToken(String)} */
    @Deprecated
    public LoginUser parseLoginUser(String token) {
        AuthUser authUser = parseToken(token);
        return new LoginUser(authUser.getUserId(), authUser.getUsername(),
                authUser.getRoleType().toLegacy());
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static RoleTypeEnum resolveRoleType(Claims claims) {
        String roleType = claims.get(CLAIM_ROLE_TYPE, String.class);
        if (roleType == null || roleType.isBlank()) {
            roleType = claims.get("role", String.class);
        }
        return RoleTypeEnum.fromValue(roleType);
    }
}
