package com.sky.utils;

import com.sky.constant.JwtClaimsConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名的时候使用的签名算法，也就是header那部分
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 设置jwt的body
        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 生成Refresh Token
     * Refresh Token通常有更长的过期时间
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createRefreshJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 为refresh token添加特殊标识
        claims.put("token_type", "refresh");
        return createJWT(secretKey, ttlMillis, claims);
    }

    /**
     * 验证Token是否即将过期
     * 
     * @param secretKey jwt秘钥
     * @param token     token
     * @param thresholdMinutes 提前多少分钟判断为即将过期
     * @return true表示即将过期或已过期
     */
    public static boolean isTokenExpiringSoon(String secretKey, String token, int thresholdMinutes) {
        try {
            Claims claims = parseJWT(secretKey, token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // 计算阈值时间
            long thresholdMillis = thresholdMinutes * 60 * 1000L;
            Date thresholdTime = new Date(now.getTime() + thresholdMillis);
            
            // 如果过期时间早于阈值时间，则认为即将过期
            return expiration.before(thresholdTime);
        } catch (Exception e) {
            // 解析失败，认为已过期
            return true;
        }
    }

    /**
     * 验证Token是否有效（未过期且格式正确）
     * 
     * @param secretKey jwt秘钥
     * @param token     token
     * @return true表示有效
     */
    public static boolean isTokenValid(String secretKey, String token) {
        try {
            Claims claims = parseJWT(secretKey, token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // 检查是否过期
            return expiration.after(now);
        } catch (Exception e) {
            // 解析失败或其他异常，认为无效
            return false;
        }
    }

    /**
     * 验证Refresh Token是否有效
     * 
     * @param secretKey jwt秘钥
     * @param refreshToken refresh token
     * @return true表示有效
     */
    public static boolean isRefreshTokenValid(String secretKey, String refreshToken) {
        try {
            Claims claims = parseJWT(secretKey, refreshToken);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // 检查是否过期
            if (expiration.before(now)) {
                return false;
            }
            
            // 检查是否是refresh token
            String tokenType = (String) claims.get("token_type");
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            // 解析失败或其他异常，认为无效
            return false;
        }
    }

    /**
     * 从Token中提取用户ID
     * 
     * @param secretKey jwt秘钥
     * @param token     token
     * @return 用户ID，解析失败返回null
     */
    public static Long extractUserId(String secretKey, String token) {
        try {
            Claims claims = parseJWT(secretKey, token);
            // 尝试不同的claim名称
            Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
            if (userIdObj == null) {
                userIdObj = claims.get("userId"); // 备用名称
            }
            
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     * 
     * @param secretKey jwt秘钥
     * @param token     token
     * @return 用户名，解析失败返回null
     */
    public static String extractUsername(String secretKey, String token) {
        try {
            Claims claims = parseJWT(secretKey, token);
            String username = (String) claims.get(JwtClaimsConstant.USERNAME);
            if (username == null) {
                username = (String) claims.get("username"); // 备用名称
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中提取用户类型
     * 
     * @param secretKey jwt秘钥
     * @param token     token
     * @return 用户类型，解析失败返回null
     */
    public static String extractUserType(String secretKey, String token) {
        try {
            Claims claims = parseJWT(secretKey, token);
            String userType = (String) claims.get(JwtClaimsConstant.USER_TYPE);
            if (userType == null) {
                userType = (String) claims.get("userType"); // 备用名称
            }
            return userType;
        } catch (Exception e) {
            return null;
        }
    }
}
