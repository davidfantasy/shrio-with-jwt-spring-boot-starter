package com.github.davidfantasy.jwtshiro;


import java.util.Set;

/**
 * 用于存储登录用户验证权限和生成token所需要的必要信息，由具体的应用程序通过实现AuthUserService接口获得
 **/
public class UserInfo {

    private String account;

    private String password;

    private Set<String> permissions;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

}
