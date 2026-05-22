package com.example.yupicturebackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 给数据库实现分页功能
 * 1. 用户请求：查询第2页，每页10条
 *    ↓
 * 2. Controller 创建 Page 对象：new Page<>(2, 10)
 *    ↓
 * 3. Service 调用 pictureMapper.selectPage(page, ...)
 *    ↓
 * 4. 分页拦截器拦截这个查询
 *    ↓
 * 5. 拦截器自动在 SQL 后面加上 LIMIT 10, 10
 *    ↓
 * 6. 执行查询，只返回第2页的10条数据
 *    ↓
 * 7. 返回给前端，同时带上总记录数、总页数等信息
 */
@Configuration
@MapperScan("com.example.yupicturebackend.mapper")
public class MyBatisPlusConfig {
    /**
     * 拦截器配置 用于实现分页功能
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        //创建实例
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //分页插件 添加MySQL数据库的分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
//        返回实例
        return interceptor;
    }
}
