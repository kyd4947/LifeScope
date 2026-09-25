package com.lifescope.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * DevTools 가 runtime classpath 에 있는지 판별한다.
 *
 * <p>DevTools 가 있으면 소스 변경마다 RestartClassLoader 가 재생성되어
 * JDK 직렬화 Redis 캐시를 역직렬화할 때 ClassCastException 이 발생한다.
 * 개발 환경은 {@link DevNoCacheConfig} 가 인메모리 캐시를 사용한다.
 */
public class DevtoolsDetector {

	private DevtoolsDetector() {
	}

	/** devtools 가 classpath 에 있으면 true */
	public static boolean isPresent() {
		try {
			Class.forName("org.springframework.boot.devtools.restart.classloader.RestartClassLoader");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/** 운영 환경(devtools 없음) 에서만 true */
	public static class NotDevtoolsCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return !isPresent();
		}
	}

	/** 개발 환경(devtools 있음) 에서만 true */
	public static class DevtoolsCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return isPresent();
		}
	}
}
