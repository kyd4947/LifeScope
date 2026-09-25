package com.lifescope.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import lombok.extern.slf4j.Slf4j;

// 운영 환경용 Redis 캐시 설정
//
// ※ 개발 환경(devtools 포함) 에서는 이 설정이 적용되지 않는다.
//   devtools 가 classloader 를 반복 재생성해 JDK 직렬화 캐시를 역직렬화하면
//   ClassCastException 이 발생한다.
//   개발 환경은 DevNoCacheConfig 가 인메모리 캐시로 대체한다.
@Slf4j
@Configuration
@EnableCaching
@Conditional(DevtoolsDetector.NotDevtoolsCondition.class)
public class CacheConfig {

	// 캐시 키 접두사
	static final String KEY_PREFIX = "lifescope:";

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		log.info("[CacheConfig] Redis 캐시 활성화 (TTL 1시간)");
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofHours(1))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()))
				.disableCachingNullValues()
				.computePrefixWith(name -> KEY_PREFIX + name + "::");
		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}
}
