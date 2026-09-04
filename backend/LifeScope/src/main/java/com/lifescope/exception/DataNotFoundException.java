package com.lifescope.exception;

// 데이터(지역 / 물가 / 임금 / 주거비)가 존재하지 않을 때 발생하는 예외 - GlobalExceptionHandler 에서 404 not Found 로 변환
public class DataNotFoundException extends RuntimeException{

	public DataNotFoundException(String message) {
		super(message);
	}
}
