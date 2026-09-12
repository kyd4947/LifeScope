package com.lifescope.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 주거비 포함 비교 응답 DTO - 주거비 차액 반영 실질 구매력
@Getter
@Builder
@AllArgsConstructor
public class HousingAdjustedResponse {

	// 기준 지역
	private final CityResponse fromCity;
	
	// 대상 지역
	private final CityResponse toCity;
	
	// 거래 유형 : M = 매매 / J = 전세 / W = 월세
	private final String tradeType;
	
	// 거래 유형 한글
	private final String tradeTypeLabel;
	
	// 입력 월급 (원)
	private final Long inputSalary;
	
	// 기준 지역 평균 주거비 (만원 - 전세는 보증금, 월세는 월세금)
	private final Long fromHousingPrice;
	
	// 대상 지역 평균 주거비 (만원)
	private final Long toHousingPrice;
	
	// 기준 지역 월 주거비 환산 (원 - 전세는 전월세 전환율 4% 적용)
	private final Long fromMonthlyHousingCost;
	
	// 대상 지역 월 주거비 환산 (원)
	private final Long toMonthlyHousingCost;
	
	// 실질 구매력 (원) - 월급에서 주거비 증가분 차감
	private final Long realPurchasingPower;
}
