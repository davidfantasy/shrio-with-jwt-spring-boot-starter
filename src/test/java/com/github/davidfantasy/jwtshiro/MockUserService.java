package com.github.davidfantasy.jwtshiro;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

@Service
public class MockUserService implements AuthUserLoader {

    @Override
    public UserInfo getUserInfo(String account) {
        UserInfo user = new UserInfo();
        user.setAccount("aUser");
        user.setPassword("123456");
        user.setPermissions(Sets.newHashSet("test-perm:" + AnnotationFilterRuleLoader.BASE_AUTH_PERM_NAME, "test-perm:perm1"));
        return user;
    }

}