package com.lifescope.config;

import java.time.Duration;
import java.util.function.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

// 공공 API(공공데이터 포털 KOSIS/MOLIT) 호출 내성 설정 - 스타터 대신 코어 모듈 프로그래매틱 사용
@Configuration
public class ResilienceConfig {

	// 재시도 / 실패 카운트 대상 : 네트워크 오류 또는 서버(5xx) 오류만. 4xx, 빈 키 등은 제외
	private static final Predicate<Throwable> RETRYABLE = ex ->
		(ex instanceof ResourceAccessException)
		|| (ex instanceof RestClientResponseException r && r.getStatusCode().is5xxServerError());
		
	@Bean
	public Retry publicApiRetry() {
		RetryConfig config = RetryConfig.custom()
				.maxAttempts(3)
				.waitDuration(Duration.ofSeconds(1))
				.retryOnException(RETRYABLE)
				.build();
		return Retry.of("public-api", config);
	}
	
	@Bean
	public CircuitBreaker publicApiCircuitBreaker() {
		CircuitBreakerConfig config = CircuitBreakerConfig.custom()
				.failureRateThreshold(50)
				.slidingWindowSize(10)
				.minimumNumberOfCalls(5)
				.waitDurationInOpenState(Duration.ofSeconds(30))
				.recordException(RETRYABLE)
				.build();
		return CircuitBreaker.of("public-api", config);
	}
}
