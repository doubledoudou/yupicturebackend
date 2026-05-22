package com.example.yupicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 自定义注解AuthCheck，用于权限校验
 * 该注解通常配合拦截器或者AOP使用，用于检查当前角色是否具有执行某个方法所需的权限
 * 贴在方法上 → ElementType.METHOD
 * 贴在类上 → ElementType.TYPE
 * 贴在变量上 → ElementType.FIELD
 * 贴在参数上 → ElementType.PARAMETER
 */
//这两个都是元注解
@Target(ElementType.METHOD)   // 指定注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 必须有某个角色 用于指定必须拥有的角色，默认为空字符串
     */
    String mustRole() default "";
}
