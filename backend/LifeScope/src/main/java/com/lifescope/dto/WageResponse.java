package com.lifescope.dto;

import com.lifescope.domain.wage.AverageWage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 평균 임금 응답 DTO
@Getter
@Builder
@AllArgsConstructor
public class WageResponse {

	// 지역 코드
	private final String cityCode;
	
	// 지역명
	private final String cityName;
	
	// 기준 연도
	private final Short year;
	
	// 연 평균 임금 (원)
	private final Long wageAvg;
	
	// 월 평균 임금 (원)
	private final Long wageMonthly;
	
	// 엔티티 -> DTO 변환
	public static WageResponse from(AverageWage wage) {
		return WageResponse.builder()
				.cityCode(wage.getCity().getCode())
				.cityName(wage.getCity().getName())
				.year(wage.getYear())
				.wageAvg(wage.getWageAvg())
				.wageMonthly(wage.getWageMonthly())
				.build();
	}
}
