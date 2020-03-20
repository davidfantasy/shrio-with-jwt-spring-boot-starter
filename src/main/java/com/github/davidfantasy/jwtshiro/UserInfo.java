package com.github.davidfantasy.jwtshiro;


import java.util.Set;

/**
 * 用于存储登录用户验证权限和生成token所需要的必要信息，由具体的应用程序通过实现AuthUserService接口获得
 **/
public class UserInfo {

    /**
     * 用户的唯一标识
     */
    private String account;

    /**
     * accessToken的密钥，用于对accessToken进行加密和解密
     * 建议为每个用户配置不同的密钥（比如使用用户的password）
     */
    private String secret;

    /**
     * 用户权限集合，含义类似于Shiro中的perms
     */
    private Set<String> permissions;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
