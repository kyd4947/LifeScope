package com.lifescope.exception;

// 유효하지 않은 거래 유형 (매매 / 전세 / 월세 외) 요청됐을 때 발생하는 예외 - GlobalExceptionHandler 에서 400 Bad Request 로 변환
public class InvalidTradeTypeException extends RuntimeException{

	public InvalidTradeTypeException(String message) {
		super(message);
	}
}
