package com.lifescope.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 지역 간 생활비 종합 비교 결과 DTO (메인 기능 응답) - 데이터가 없는 경우 null 처리
@Getter
@Builder
@AllArgsConstructor
public class ComparisonResult {

	// 기준 지역
	private final CityResponse fromCity;
	
	// 대상 지역 (이사 가정 지역)
	private final CityResponse toCity;
	
	// 물가 비율
	private final BigDecimal cpiRatio;
	
	// 입력받은 원래 월급
	private final Long inputMonthlySalary;
	
	// 물가 보정 환산 월급
	private final Long adjustedMonthlySalary;
	
	// 임금 비교 (한 쪽이라도 데이터가 없는 경우 null)
	private final WageComparison wage;
	
	// 주거비(전세 기준) 비교 (한 쪽이라도 데이터가 없는 경우 null)
	private final HousingComparison housing;
	
	/** 임금 비교 정보 */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class WageComparison{
		
		// 기준 지역 임금
		private final WageResponse fromWage;
		
		// (이사) 대상 지역 임금
		private final WageResponse toWage;
		
		// 임금 비율
		private final BigDecimal ratio;
	}
	
	/** 주거비 비교 정보 */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class HousingComparison{
		
		// 기준 지역 주거비 (전세)
		private final HousingPriceResponse fromPrice;
		
		// (이사) 대상 지역 주거비 (전세)
		private final HousingPriceResponse toPrice;
		
		// 주거비 비율
		private final BigDecimal ratio;
	}
}
