package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.shiro.JWTPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 由应用端负责实现该接口，以适配业务端的实际情况
 **/
public interface JWTUserAuthService {

    /**
     * 根据用户的唯一标示对用户进行认证，并获取用户的权限等信息
     * 如果account对应的用户信息不存在，应返回null
     * @param account 用户的唯一标示
     * @return 该用户所拥有的权限信息
     */
    UserInfo getUserInfo(String account);

    /**
     * 自定义访问资源认证失败时的处理方式，例如返回json格式的错误信息
     * {\"code\":401,\"message\":\"用户认证失败！\")
     */
    void onAuthenticationFailed(HttpServletRequest req, HttpServletResponse res);

    /**
     * 自定义访问资源权限不足时的处理方式，例如返回json格式的错误信息
     * {\"code\":403,\"message\":\"permission denied！\")
     */
    void onAuthorizationFailed(HttpServletRequest req, HttpServletResponse res);

    /**
     * 获取当前token携带的用户信息
     *
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
