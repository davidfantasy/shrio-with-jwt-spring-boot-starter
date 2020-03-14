package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import com.github.davidfantasy.jwtshiro.annotation.RequiresPerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jwttest")
@RequiresPerms("test-perm")
public class MockController {

    @Autowired
    private MockUserService userService;

    @Autowired
    private JWTHelper jwtHelper;

    @AlowAnonymous
    @PostMapping("/login")
    public Result login() {
        UserInfo user = userService.getUserInfo("");
        String token = jwtHelper.sign(user.getAccount(), user.getPassword());
        //后续token的刷新由客服端负责维护
        Result result = new Result();
        result.setToken(token);
        return result;
    }

    @GetMapping("/anon")
    @AlowAnonymous
    public String testAnon() {
        return "0";
    }

    @GetMapping("/user")
    public String testUser() {
        return "0";
    }

    @GetMapping("/perm1")
    @RequiresPerms("perm1")
    public String testPerm1() {
        return "0";
    }

    @GetMapping("/perm2")
    @RequiresPerms("perm2")
    public String testPerm2() {
        return "0";
    }

}
