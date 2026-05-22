package com.example.yupicturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**") //对所有API路径生效
                // 允许发送 Cookie
                .allowCredentials(true)    //允许携带 Cookie等凭证信息
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")  //允许所有域名访问
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")   //允许的请求方法
                .allowedHeaders("*")   //允许所有请求头
                .exposedHeaders("*");   //暴露所有响应给客户端
    }
}

