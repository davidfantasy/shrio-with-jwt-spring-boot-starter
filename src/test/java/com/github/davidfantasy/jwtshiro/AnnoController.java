package com.github.davidfantasy.jwtshiro;


import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doc")
@AlowAnonymous
public class AnnoController {


    @GetMapping("/preview")
    public String test() {
        return "0";
    }

}
