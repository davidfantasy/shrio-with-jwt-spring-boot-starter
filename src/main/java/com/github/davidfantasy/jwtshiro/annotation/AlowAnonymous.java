package com.github.davidfantasy.jwtshiro.annotation;

import java.lang.annotation.*;

/**
 * 用于标注无需登录即可访问的链接
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AlowAnonymous {
}
