package com.github.davidfantasy.jwtshiro;

import com.google.common.collect.Sets;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class MockJWTUserAuthService implements JWTUserAuthService {

    @Override
    public UserInfo getUserInfo(String account) {
        UserInfo user = new UserInfo();
        user.setAccount("aUser");
        user.setSecret("123456");
        user.setPermissions(Sets.newHashSet("" +
                "parent-perm:" + AnnotationFilterRuleLoader.BASE_AUTH_PERM_NAME,
                "parent-perm:subperm-1",
                "parent-perm:pathvar-perm-2"));
        return user;
    }


    @Override
    public void onAuthenticationFailed(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public void onAuthorizationFailed(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpStatus.FORBIDDEN.value());
    }

}