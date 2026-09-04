package com.lifescope.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 모든 에러 응답 공통 형식 - 어떤 에러가 나도 동일한 구조 응답 -> 프론트에서 일관된 에러 처리 가능
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
	
	// HTTP 상태 코드
	private final int status;
	
	// HTTP 에러명
	private final String error;
	
	// 에러 상세 메세지
	private final String message;
	
	// 에러 발생 시각
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime timestamp;
	
	// 요청 경로
	private final String path;
	
	// 정적 팩토리 - 상태 코드 / 메세지 / 경로 응답 생성
	public static ErrorResponse of(HttpStatus status, String message, String path) {
		return ErrorResponse.builder()
				.status(status.value())
				.error(status.getReasonPhrase())
				.message(message)
				.timestamp(LocalDateTime.now())
				.path(path)
				.build();
	}

}
