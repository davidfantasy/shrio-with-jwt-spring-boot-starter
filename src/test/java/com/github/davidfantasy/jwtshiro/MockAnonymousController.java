package com.github.davidfantasy.jwtshiro;


import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟业务接口，用于测试匿名访问
 */
@RestController
@RequestMapping("/api/doc")
@AlowAnonymous
public class MockAnonymousController {


    @GetMapping("/preview")
    public String test() {
        return "0";
    }

}
