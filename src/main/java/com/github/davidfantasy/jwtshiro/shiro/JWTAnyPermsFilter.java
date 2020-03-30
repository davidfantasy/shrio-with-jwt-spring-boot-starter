package com.github.davidfantasy.jwtshiro.shiro;

import com.github.davidfantasy.jwtshiro.JWTUserAuthService;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * shiro自定义过滤器，允许拥有任何一个指定权限的用户访问受保护资源的filter
 */
public class JWTAnyPermsFilter extends PermissionsAuthorizationFilter {

    private JWTUserAuthService userAuthService;

    public JWTAnyPermsFilter(JWTUserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        this.userAuthService.onAuthorizationFailed((HttpServletRequest) request, (HttpServletResponse) response);
        return false;
    }

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        Subject subject = getSubject(request, response);
        String[] perms = (String[]) mappedValue;
        if (perms != null && perms.length > 0) {
            boolean isPermitted = false;
            for (String perm : perms) {
                if (subject.isPermitted(perm)) {
                    isPermitted = true;
                    break;
                }
            }
            return isPermitted;
        } else {
            return true;
        }
    }
}
