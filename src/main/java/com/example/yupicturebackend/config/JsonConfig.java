package com.example.yupicturebackend.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json 配置
 * 翻译官：java对象转JSON
 * 1. 项目启动
 *    ↓
 * 2. Spring 扫描到 @JsonComponent
 *    ↓
 * 3. 执行 jacksonObjectMapper() 方法
 *    ↓
 * 4. 创建 ObjectMapper，注册 Long→String 规则
 *    ↓
 * 5. 注册为 Bean，替换默认的 ObjectMapper
 *    ↓
 * 6. Controller 返回数据时，自动使用新规则
 *    ↓
 * 7. 前端收到的 JSON 中，Long 类型都变成字符串
 */
@JsonComponent
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     * 为了解决 Java 中 Long 类型在转换成 JSON 时可能会出现的精度丢失问题。
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 1. 创建一个不支持 XML 的 ObjectMapper
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
//        创建一个 SimpleModule 模块 用于注册序列化规则
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 4. 将模块注册到 ObjectMapper
        objectMapper.registerModule(module);
        // 5. 返回配置好的 ObjectMapper
        return objectMapper;
    }
}

