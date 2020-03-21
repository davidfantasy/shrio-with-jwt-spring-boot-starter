package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import com.github.davidfantasy.jwtshiro.annotation.RequiresPerms;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

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
        UserInfo user = userService.getUserInfo("testUser");
        String token = jwtHelper.sign(user.getAccount(), user.getSecret());
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

    @ExceptionHandler(value = {AuthenticationException.class})
    public void authcExceptionHandler(Exception e, HttpServletResponse res) {
        responseResult(res, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public void authoExceptionHandler(Exception e, HttpServletResponse res) {
        responseResult(res, HttpStatus.FORBIDDEN);
    }

    private void responseResult(HttpServletResponse response, HttpStatus status) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-type", "application/json;charset=UTF-8");
        response.setStatus(status.value());
    }

}
