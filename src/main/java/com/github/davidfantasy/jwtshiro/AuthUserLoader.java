package com.github.davidfantasy.jwtshiro;

/**
 * @description: 由应用端负责实现，通过账户获取用户实际的信息
 * @author: wan.yu
 **/
public interface AuthUserLoader {

    UserInfo getUserInfo(String account);

}
