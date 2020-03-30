package com.github.davidfantasy.jwtshiro.shiro;

import com.github.davidfantasy.jwtshiro.JWTUserAuthService;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * shiro自定义过滤器，拥有所有指定权限的用户才能访问受保护资源的filter
 */
public class JWTPermsFilter extends PermissionsAuthorizationFilter {

    private JWTUserAuthService userAuthService;

    public JWTPermsFilter(JWTUserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        this.userAuthService.onAuthorizationFailed((HttpServletRequest) request, (HttpServletResponse) response);
        return false;
    }

}
