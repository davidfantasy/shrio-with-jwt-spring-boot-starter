package com.github.davidfantasy.jwtshiro;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = JWTShiroProperties.JWT_SHIRO_PREFIX)
public class JWTShiroProperties {

    public static final String JWT_SHIRO_PREFIX = "jwt-shiro";

    /**
     * 需要进行权限拦截的URL pattern, 多个使用url隔开，例如：/api/*,/rest/*
     * URL pattern的规则和http filter一致
     */
    private String urlPattern = "/*";

    /**
     * accessToken的理论过期时间，单位分钟，token如果超过该时间则接口响应的header中附带新的token信息
     */
    private int maxAliveMinute = 30;

    /**
     * accessToken的最大生存周期，单位分钟，在此时间内的token无需重新登录即可刷新
     */
    private int maxIdleMinute = 60;

    /**
     * accessToken在http header中的name
     */
    private String headerKeyOfToken = "jwt-token";

    /**
     * token中保存的用户名的key name
     */
    private String accountAlias = "account";

    /**
     * 是否启用token的自动刷新机制
     */
    private boolean enableAutoRefreshToken = false;

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

    public boolean isEnableAutoRefreshToken() {
        return enableAutoRefreshToken;
    }

    public void setEnableAutoRefreshToken(boolean enableAutoRefreshToken) {
        this.enableAutoRefreshToken = enableAutoRefreshToken;
    }

    @Override
    public String toString() {
        return "JWTShiroProperties{" +
                "urlPattern='" + urlPattern + '\'' +
                ", maxAliveMinute=" + maxAliveMinute +
                ", maxIdleMinute=" + maxIdleMinute +
                ", headerKeyOfToken='" + headerKeyOfToken + '\'' +
                ", accountAlias='" + accountAlias + '\'' +
                ", enableAutoRefreshToken=" + enableAutoRefreshToken +
                '}';
    }
    
}
