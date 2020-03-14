package com.github.davidfantasy.jwtshiro;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JWTShiroProperties.JWT_SHIRO_PREFIX)
public class JWTShiroProperties {

    public static final String JWT_SHIRO_PREFIX = "jwt-shiro";

    /**
     * 权限生效的URL pattern, 多个使用url隔开，例如：/api/*,/rest/*
     */
    private String urlPattern = "/*";

    /**
     * JWT token的过期时间，单位分钟
     */
    private int maxAliveMinute = 30;

    /**
     * 最多允许用户多长时间不操作后,无需再次登录仍然可以刷新token 单位分钟
     */
    private int maxIdleMinute = 60;

    /**
     * 需要刷新token时，后端response携带的token在http header中的name
     */
    private String headerKeyOfToken = "JWT-TOKEN";

    /**
     * token中保存的用户名的key name
     */
    private String accountAlias = "account";

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public int getMaxAliveMinute() {
        return maxAliveMinute;
    }

    public void setMaxAliveMinute(int maxAliveMinute) {
        this.maxAliveMinute = maxAliveMinute;
    }

    public int getMaxIdleMinute() {
        return maxIdleMinute;
    }

    public void setMaxIdleMinute(int maxIdleMinute) {
        this.maxIdleMinute = maxIdleMinute;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public void setAccountAlias(String accountAlias) {
        this.accountAlias = accountAlias;
    }

    public String getHeaderKeyOfToken() {
        return headerKeyOfToken;
    }

    public void setHeaderKeyOfToken(String headerKeyOfToken) {
        this.headerKeyOfToken = headerKeyOfToken;
    }

    @Override
    public String toString() {
        return "{" +
                "urlPattern='" + urlPattern + '\'' +
                ", maxAliveMinute=" + maxAliveMinute +
                ", maxIdleMinute=" + maxIdleMinute +
                ", headerKeyOfToken='" + headerKeyOfToken + '\'' +
                ", accountAlias='" + accountAlias + '\'' +
                '}';
    }
}
