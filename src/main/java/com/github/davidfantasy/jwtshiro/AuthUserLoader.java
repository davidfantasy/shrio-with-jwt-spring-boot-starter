package com.github.davidfantasy.jwtshiro;

/**
 * 由应用端负责实现，通过账户获取用户实际的信息
 **/
public interface AuthUserLoader {

    UserInfo getUserInfo(String account);

}
