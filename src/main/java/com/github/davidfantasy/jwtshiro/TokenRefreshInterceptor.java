package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.shiro.JWTPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * SpringMVC拦截器，用于检查即将过期的token并及时返回token更新
 **/
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private AuthUserLoader userLoader;

    private JWTHelper jwtHelper;

    private String headerKeyOfToken;

    protected TokenRefreshInterceptor(AuthUserLoader userLoader, JWTHelper jwtHelper, String headerKeyOfToken) {
        this.jwtHelper = jwtHelper;
        this.userLoader = userLoader;
        this.headerKeyOfToken = headerKeyOfToken;
    }

    /**
     * 涉及修改response header的操作只能放到preHandle中，postHandle中的response信息已经被锁定，无法修改
     **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Subject subject = SecurityUtils.getSubject();
        JWTPrincipal principal = (JWTPrincipal) subject.getPrincipal();
        //当前的token已经过期但还未超过maxIdleMinute的情况
        if (principal != null && System.currentTimeMillis() > principal.getExpiresAt()) {
            UserInfo user = userLoader.getUserInfo(principal.getAccount());
            String newToken = jwtHelper.sign(user.getAccount(), user.getSecret());
            response.setHeader(headerKeyOfToken, newToken);
        }
        return true;
    }

}
