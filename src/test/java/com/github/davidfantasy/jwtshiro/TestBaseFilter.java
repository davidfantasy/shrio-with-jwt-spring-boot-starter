package com.github.davidfantasy.jwtshiro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
//注意，TestPropertySource默认不能解析yml文件，只能使用property格式的文件
@TestPropertySource("classpath:application-test.property")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class TestBaseFilter {

    @Value("${jwt-shiro.header-key-of-token}")
    private String headerKeyOfToken = "JWT-TOKEN";

    @Autowired
    private MockMvc mvc;

    private String token;

    @Before
    public void login() throws Exception {
        MvcResult result = this.mvc.perform(post("/jwttest/login")).andExpect(status().isOk()).andReturn();
        Result res = JsonUtil.json2obj(result.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(res.getToken());
        this.token = res.getToken();
    }

    @Test
    public void testAnno() throws Exception {
        this.mvc.perform(get("/jwttest/anon")).andExpect(status().isOk()).andExpect(content().string("0"));
    }

    @Test
    public void testAnnoClass() throws Exception {
        this.mvc.perform(get("/api/doc/preview"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testUser() throws Exception {
        this.mvc.perform(get("/jwttest/user"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
                //.andExpect(content().string("{\"code\":401,\"message\":\"用户认证失败\",\"data\":null}"));
        this.mvc.perform(get("/jwttest/user").
                header(headerKeyOfToken, token))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void testPerm() throws Exception {
        this.mvc.perform(get("/jwttest/perm1")
                .header(headerKeyOfToken, token))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
        this.mvc.perform(get("/jwttest/perm2")
                .header(headerKeyOfToken, token))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
                //.andExpect(content().string("{\"code\":403,\"message\":\"permission denied！\",\"data\":null}"));
    }

}
