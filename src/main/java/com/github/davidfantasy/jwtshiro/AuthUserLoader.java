package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.shiro.JWTPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

/**
 * 由应用端负责实现，通过账户获取用户实际的信息
 **/
public interface AuthUserLoader {

    UserInfo getUserInfo(String account);

    /**
     * 获取当前token携带的用户信息
     * @param throwException 没有获取到用户时是否抛出异常
     * @return 当前用户信息
     */
    default UserInfo getAuthenticatedUser(boolean throwException) {
        Subject subject = SecurityUtils.getSubject();
        JWTPrincipal userPrincipal = (JWTPrincipal) subject.getPrincipal();
        if (userPrincipal != null) {
            return (UserInfo) this.getUserInfo(userPrincipal.getAccount());
        } else if (throwException) {
            throw new IllegalStateException("无法获取当前用户信息");
        }
        return null;
    }

}
