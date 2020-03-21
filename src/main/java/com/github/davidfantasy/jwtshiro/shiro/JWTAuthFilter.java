package com.github.davidfantasy.jwtshiro.shiro;

import com.github.davidfantasy.jwtshiro.JWTUserAuthService;
import com.google.common.base.Strings;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * shiro自定义过滤器，用于对用户的JWT TOKEN进行基础验证
 */
public class JWTAuthFilter extends AccessControlFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);

    private String headerKeyOfToken;

    private JWTUserAuthService userAuthService;

    public JWTAuthFilter(String headerKeyOfToken, JWTUserAuthService userAuthService) {
        this.headerKeyOfToken = headerKeyOfToken;
        this.userAuthService = userAuthService;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        //从header或URL参数中查找token
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(headerKeyOfToken);
        if (Strings.isNullOrEmpty(authorization)) {
            authorization = req.getParameter(headerKeyOfToken);
        }
        JWTToken token = new JWTToken(authorization);
        try {
            getSubject(request, response).login(token);
        } catch (Exception e) {
            logger.error("认证失败:" + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        this.userAuthService.onAuthenticationFailed((HttpServletRequest) request, (HttpServletResponse) response);
        return false;
    }

}
