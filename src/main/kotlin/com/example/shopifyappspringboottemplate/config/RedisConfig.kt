package com.example.shopifyappspringboottemplate.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> = ReactiveRedisTemplate(
        factory,
        RedisSerializationContext
            .newSerializationContext<String, String>(
                StringRedisSerializer()
            )
            .hashKey(StringRedisSerializer())
            .hashValue(StringRedisSerializer())
            .build()
    )
}
