package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JsonHttpMessageConverterConfig {


    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        Object o = new Object();
        list.add(o);
        list.add(o);
        System.out.println(list.size());
        System.out.println(JSONObject.toJSONString(list));
        System.out.println(JSONObject.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect));
    }


    /**
     * 对数据类型进行转换的一个工具类，具有高的优先级。
     */
    @Bean
    @Primary
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //序列化配置
        fastJsonConfig.setSerializerFeatures(
                //格式化输出 -> 按照标准json格式进行输出
                SerializerFeature.PrettyFormat,
                //没有string就是空
                SerializerFeature.WriteNullStringAsEmpty,
                //如果list没有就是空的字符串
                SerializerFeature.WriteNullListAsEmpty,
                //如果map没有就是空的字符串
                SerializerFeature.WriteMapNullValue,
                //map的键值对进行一个排序 默认升序
                SerializerFeature.MapSortField,
                //禁用循环引用
                SerializerFeature.DisableCircularReferenceDetect
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return new HttpMessageConverters(fastConverter);
    }
}
