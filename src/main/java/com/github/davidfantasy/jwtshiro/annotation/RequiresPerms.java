package com.github.davidfantasy.jwtshiro.annotation;

import org.apache.shiro.authz.annotation.Logical;

import java.lang.annotation.*;

/**
 * 用于标注某个URL对应的具体的permission
 * @author david
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPerms {

    /**
     * 用于判断用户是否可以访问当前接口的权限字符串
     *
     * @since 1.0.4
     */
    String[] value();

    /**
     * 当指定多个权限字符串时，进行权限检查时的逻辑操作类型，默认是AND
     *
     * @since 1.0.4
     */
    Logical logical() default Logical.AND;

}
