package com.github.davidfantasy.jwtshiro.shiro;

import com.github.davidfantasy.jwtshiro.JWTUserAuthService;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWTPermFilter extends PermissionsAuthorizationFilter {

    private JWTUserAuthService userAuthService;

    public JWTPermFilter(JWTUserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        this.userAuthService.onAuthorizationFailed((HttpServletRequest) request, (HttpServletResponse) response);
        return false;
    }

}
