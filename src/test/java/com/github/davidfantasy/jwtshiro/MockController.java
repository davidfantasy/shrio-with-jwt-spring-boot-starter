package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import com.github.davidfantasy.jwtshiro.annotation.RequiresPerms;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟实际的业务接口，用于单元测试
 */
@RestController
@RequestMapping("/auth-test")
@RequiresPerms("parent-perm:basic")
public class MockController {

    @Autowired
    private MockJWTUserAuthService userService;

    @Autowired
    private JWTHelper jwtHelper;

    @AlowAnonymous
    @PostMapping("/login")
    public Result login() {
        UserInfo user = userService.getUserInfo("testUser");
        String token = jwtHelper.sign(user.getAccount(), user.getSecret());
        //后续token的刷新由客服端负责维护
        Result result = new Result();
        result.setToken(token);
        return result;
    }

    @GetMapping("/anon")
    @AlowAnonymous
    public String testAnonymous() {
        return "ok";
    }

    @GetMapping("/action-1")
    public String testRequireParentPerm() {
        return "ok";
    }

    @GetMapping("/action-2")
    @RequiresPerms("parent-perm:subperm-1")
    public String testRequireSubperm1() {
        return "ok";
    }

    @GetMapping("/action-3")
    @RequiresPerms("parent-perm:subperm-2")
    public String testRequireSubperm2() {
        return "ok";
    }

    @GetMapping("/pathvar/{id}")
    @RequiresPerms("parent-perm:pathvar-perm-1")
    public String testPathvar1() {
        return "ok";
    }

    @GetMapping("/deep-pathvar/{id}/action1")
    @RequiresPerms("parent-perm:pathvar-perm-2")
    public String testPathvar2() {
        return "ok";
    }

    @GetMapping("/deep-pathvar/{id}/action2")
    @RequiresPerms("parent-perm:pathvar-perm-3")
    public String testPathvar3() {
        return "ok";
    }

    @GetMapping("/multi-perms/and")
    @RequiresPerms({"parent-perm:subperm-1", "parent-perm:subperm-2"})
    public String testMultiPermsAnd() {
        return "ok";
    }

    @GetMapping("/multi-perms/or")
    @RequiresPerms(value = {"parent-perm:subperm-1", "parent-perm:subperm-2"}, logical = Logical.OR)
    public String testMultiPermsOr() {
        return "ok";
    }


}
