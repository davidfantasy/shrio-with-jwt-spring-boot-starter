package com.github.davidfantasy.jwtshiro.annotation;

import java.lang.annotation.*;

/**
 * 用于标注某个URL对应的具体的permission
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPerms {
    
    String value();

}
