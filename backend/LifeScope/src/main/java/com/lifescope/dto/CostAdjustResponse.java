package com.lifescope.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 물가 보정 환산 응답 DTO
@Getter
@Builder
@AllArgsConstructor
public class CostAdjustResponse {

	// 기준 지역 (현재 거주지)
	private final CityResponse fromCity;
	
	// 대상 지역 (이주 가정 지역)
	private final CityResponse toCity;
	
	// 물가 비율 (대상 / 기준 CPI)
	private final BigDecimal cpiRatio;
	
	// 입력 월급 (원)
	private final Long inputSalary;
	
	// 물가 보정 환산 월급 (원) - 대상 지역에서 동등 생활 수준 필요 금액
	private final Long adjustedSalary;
}
