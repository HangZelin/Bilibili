package com.imooc.bilibili.service.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.imooc.bilibili.domain.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {

    private static final String ISSUER = "hyddd"; //签发者

    public static String generateToken(Long userId) throws Exception{
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // 如果过了三十秒，那么这个token就过期了。
        calendar.add(Calendar.SECOND, 30);
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER) //签发者
                .withExpiresAt(calendar.getTime()) //过期时间
                .sign(algorithm); //签名算法
    }

    public static Long verifyToken(String token)  {
        // 不能直接抛出异常，前端不能进行进一步的操作，比如令牌过期以某种特殊形式返回状态码其他消息。
        try {
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getKeyId();

            return Long.valueOf(userId);
        } catch (TokenExpiredException e) {
            throw new ConditionException("555", "token已过期!");
        } catch (Exception e) {
            throw new ConditionException("非法用户token!");
        }
    }

    public static String generateRefreshToken(Long id) throws Exception{
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // 如果过了三十秒，那么这个token就过期了。
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        return JWT.create().withKeyId(String.valueOf(id))
                .withIssuer(ISSUER) //签发者
                .withExpiresAt(calendar.getTime()) //过期时间
                .sign(algorithm); //签名算法
    }
}
