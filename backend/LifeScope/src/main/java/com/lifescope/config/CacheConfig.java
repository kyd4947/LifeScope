package com.lifescope.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Redis 캐시 활성화 - 값 직렬화는 JDK 기본 값 사용
@Configuration
@EnableCaching
public class CacheConfig {

}
