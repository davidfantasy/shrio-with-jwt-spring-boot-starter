package com.github.davidfantasy.jwtshiro.shiro;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.davidfantasy.jwtshiro.JWTUserAuthService;
import com.github.davidfantasy.jwtshiro.JWTHelper;
import com.github.davidfantasy.jwtshiro.UserInfo;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JWTShiroRealm extends AuthorizingRealm {

    private JWTUserAuthService userAuthService;

    private JWTHelper jwtHelper;

    public JWTShiroRealm(JWTUserAuthService userAuthService, JWTHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
        this.userAuthService = userAuthService;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 用于获取用户权限（role,permissions）,只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        JWTPrincipal principal = (JWTPrincipal) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
        UserInfo up = userAuthService.getUserInfo(principal.getAccount());
        if (up != null && up.getPermissions() != null) {
            authInfo.addStringPermissions(up.getPermissions());
        }
        return authInfo;
    }

    /**
     * 调用subject.login时触发此方法，用于验证token的正确性
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String username = jwtHelper.getAccount(token);
        if (username == null) {
            throw new AuthenticationException("无效的请求");
        }
        UserInfo user = userAuthService.getUserInfo(username);
        if (user == null) {
            throw new AuthenticationException("未找到用户信息");
        }
        DecodedJWT jwt = jwtHelper.verify(token, username, user.getSecret());
        if (jwt == null) {
            throw new AuthenticationException("token已经过期，请重新登录");
        }
        JWTPrincipal principal = new JWTPrincipal();
        principal.setAccount(user.getAccount());
        principal.setExpiresAt(jwt.getExpiresAt().getTime());
        //这里实际上会将AuthenticationToken.getCredentials()与传入的第二个参数credentials进行比较
        //第一个参数是登录成功后，可以通过subject.getPrincipal获取
        return new SimpleAuthenticationInfo(principal, token, this.getName());
    }
}
