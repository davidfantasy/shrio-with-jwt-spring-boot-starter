package com.github.davidfantasy.jwtshiro.shiro;

/**
 * @description: 用于存储登录用户的信息，可通过SecurityUtils.getSubject().getPrincipal获得
 * @author: wan.yu
 **/
public class JWTPrincipal {

    private String account;

    private long expiresAt;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

}
