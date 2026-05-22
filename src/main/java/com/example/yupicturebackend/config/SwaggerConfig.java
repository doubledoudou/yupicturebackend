package com.example.yupicturebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Swagger 配置类
 * 用于配置 Knife4j OpenAPI 2 的文档生成规则
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    /**
     * 配置 Docket，用于定义 API 文档的生成规则
     * 重点：通过 directModelSubstitute 将 Long 类型映射为 String 类型
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                // 关键配置：将所有 Long 类型的字段在 Swagger 文档中显示为 String
                .directModelSubstitute(Long.class, String.class)
                .apiInfo(new ApiInfoBuilder()
                        .title("图片后端 API")
                        .description("基于 Spring Boot + MyBatis Plus + Knife4j 构建的图片管理系统")
                        .version("1.0")
                        .contact(new Contact("yu-picture-backend", "", "example@example.com"))
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.yupicturebackend.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
