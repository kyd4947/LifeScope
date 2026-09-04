package com.lifescope.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

// 전역 예외 처리 (공통 예외 처리) - 모든 Controller 에서 발생한 예외 가로채 HTTP 상ㅌ애 코드가 매핑된 공통 ErrorResponse 변환
//						   - Controller / Service 어디에서도 try-catch 불필요
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// 404 : 데이터 없음 (지역 코드, CPI, 임금 등)
	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleDataNotFound(DataNotFoundException e, HttpServletRequest request){
		return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
	}
	
	// 400 : 잘못된 거래 유형 (M/J/W 외)
	@ExceptionHandler(InvalidTradeTypeException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTradeType(InvalidTradeTypeException e, HttpServletRequest request){
		return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
	}
	
	// 400 : 잘못된 인자 (Service의 입력 값 검증 실패 등)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request){
		return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
	}
	
	// 400 : 필수 파라미터 누락
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest request){
		String message = "필수 파라미터가 누락되었습니다 : " + e.getParameterName();
		return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
	}
	
	// 400 : 파라미터 타입 불일치
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request){
		String message = "파라미터 형식이 올바르지 않습니다 : " + e.getName();
		return buildResponse(HttpStatus.BAD_REQUEST, message, request);
	}
	
	// 500 : 위에서 못 받은 모든 예외 (예상 못한 서버 오류)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request){
		// 원인 추적 위한 로그
		log.error("서버 내부 오류 발생 - path : {}", request.getRequestURI(), e);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request);
	}
	
	// 공통 응답 조립
	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request){
		return ResponseEntity
				.status(status)
				.body(ErrorResponse.of(status, message, request.getRequestURI()));
	}
}
