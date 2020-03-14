package com.github.davidfantasy.jwtshiro.shiro;

import com.github.davidfantasy.jwtshiro.JsonUtil;
import com.github.davidfantasy.jwtshiro.HttpResult;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWTPermFilter extends PermissionsAuthorizationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthFilter.class);

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        HttpResult result = new HttpResult();
        result.setCode(HttpStatus.FORBIDDEN.value());
        result.setMessage("permission denied！");
        responseResult((HttpServletResponse) response, result);
        return false;
    }

    private void responseResult(HttpServletResponse response, HttpResult result) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
        try {
            response.getWriter().write(JsonUtil.obj2json(result));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
