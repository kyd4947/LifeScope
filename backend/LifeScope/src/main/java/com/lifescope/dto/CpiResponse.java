package com.lifescope.dto;

import java.math.BigDecimal;

import com.lifescope.domain.costOfLiving.ConsumerPriceIndex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 소비자 물가 지수(CPI) 응답 DTO -> cityName 포함 -> 프론트에서 별도 조회 불필요
@Getter
@Builder
@AllArgsConstructor
public class CpiResponse {

	// 지역 코드
	private final String cityCode;
	
	// 지역명
	private final String cityName;
	
	// 기준 년도
	private final Short baseYear;
	
	// 연/월
	private final String yearMonth;
	
	// 물가지수 값
	private final BigDecimal cpiValue;
	
	// 엔티티 -> DTO 변환
	public static CpiResponse from(ConsumerPriceIndex cpi) {
		return CpiResponse.builder()
				.cityCode(cpi.getCity().getCode())
				.cityName(cpi.getCity().getName())
				.baseYear(cpi.getBaseYear())
				.yearMonth(cpi.getYearMonth())
				.cpiValue(cpi.getCpiValue())
				.build();
	}
}
