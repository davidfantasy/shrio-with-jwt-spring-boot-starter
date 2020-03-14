package com.github.davidfantasy.jwtshiro;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JWTHelper {

    private static final Logger logger = LoggerFactory.getLogger(JWTShiroAutoConfiguration.class);

    private JWTShiroProperties prop;

    protected JWTHelper(JWTShiroProperties prop) {
        this.prop = prop;
    }

    /**
     * 校验token是否正确
     */
    public DecodedJWT verify(String token, String account, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    //在token失效前提供一个安全窗口期，使前端有机会刷新token
                    //注意这里的单位为秒
                    .acceptExpiresAt(this.prop.getMaxIdleMinute() * 60)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt;
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public String getAccount(String token) {
        if (Strings.isNullOrEmpty(token)) {
            return null;
        }
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(prop.getAccountAlias()).asString();
        } catch (JWTDecodeException e) {
            logger.error("decode token error", e);
        }
        return null;
    }

    /**
     * 生成签名
     *
     * @param account            用户名
     * @param secret             用于加密的key
     * @param expireAfterMinutes 指定token在多少分钟后过期
     * @return 加密的token
     */
    public String sign(String account, String secret, long expireAfterMinutes) {
        Date expireAfter = new Date(System.currentTimeMillis() + expireAfterMinutes * 60 * 1000);
        System.out.println(System.currentTimeMillis() + ":" + expireAfter.getTime());
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create()
                //.withIssuedAt(new Date())
                .withClaim(prop.getAccountAlias(), account)
                .withExpiresAt(expireAfter)
                .sign(algorithm);
    }

    /**
     * 生成签名
     *
     * @param account 用户名
     * @param secret  用于加密的key
     * @return 加密的token
     */
    public String sign(String account, String secret) {
        return this.sign(account, secret, prop.getMaxAliveMinute());
    }
}
