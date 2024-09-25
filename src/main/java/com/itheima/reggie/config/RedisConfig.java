package com.itheima.reggie.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author: Shinsam
 * @Date: 2024/09/25/19:56
 * @Description: Redis键String序列化配置类
 * @Notice:
 */
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //默认的Key序列化器为:JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer()) ;
        redisTemplate.setConnectionFactory (connectionFactory);
        return redisTemplate;

    }
}
