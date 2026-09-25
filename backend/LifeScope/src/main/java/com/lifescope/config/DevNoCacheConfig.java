package com.lifescope.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 개발 환경(devtools 포함) 전용 캐시 설정.
 *
 * <p>Spring Boot DevTools 는 소스 변경 시마다 RestartClassLoader 를 새로 만들어
 * 애플리케이션 클래스를 다시 로드한다. JDK 직렬화 캐시 값은 클래스명만 기록하고
 * classloader 정보는 기록하지 않으므로, 이전에 기록된 캐시를 읽으면
 * "같은 클래스명이 다른 classloader 소속" 상태가 되어 ClassCastException 이 발생한다.
 *
 * <pre>
 * class A cannot be cast to class A
 *   (A is in loader 'app'; A is in loader RestartClassLoader)
 * </pre>
 *
 * <p>따라서 개발 환경에서는 Redis 대신 인메모리 캐시({@link ConcurrentMapCacheManager})를
 * 사용한다. {@code @Cacheable} 코드는 손댈 필요 없이 그대로 동작한다.
 *
 * <p>운영 환경에서는 devtools 가 classpath 에 없어 이 설정이 비활성화되고
 * {@link CacheConfig} 가 Redis 기반 캐시를 사용한다.
 */
@Configuration
@EnableCaching
@Conditional(DevtoolsDetector.DevtoolsCondition.class)
public class DevNoCacheConfig {

	/** 개발 중에는 인메모리 캐시로 대체 (Redis 직렬화 / classloader 충돌 회피) */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(
				"cities", "towns", "city",
				"cpiLatest", "cpiHistory",
				"wageLatest", "wageByYear", "wageRanking",
				"housingLatest", "housingHistory",
				"comparison", "comparisonMulti");
	}
}
