package com.lifescope.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 다중 지역 비교 결과 DTO - cities 배열의 첫 번째가 기준 지역
//							- 데이터가 없는 항목은 null 처리 (부분 비교 가능)
@Getter
@Builder
@AllArgsConstructor
public class MultiComparisonResponse implements Serializable {

	// 기준 지역 (cities 배열의 첫 번째와 동일)
	private final CityResponse baseCity;

	// 입력받은 원래 월급
	private final Long inputMonthlySalary;

	// 비교 대상 목록 (첫 번째가 기준)
	private final List<CityComparison> cities;

	/** 지역별 비교 정보 */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class CityComparison implements Serializable {

		// 지역 정보
		private final CityResponse city;

		// 최신 CPI (데이터가 없으면 null)
		private final CpiResponse cpi;

		// 기준 대비 물가 비율 (기준 지역 = 1.0000, CPI 가 없으면 null)
		private final BigDecimal cpiRatio;

		// 기준 월급을 해당 지역 물가로 환산한 금액
		private final Long adjustedMonthlySalary;

		// 최신 임금 (데이터가 없으면 null)
		private final WageResponse wage;

		// 기준 대비 임금 비율 (기준 지역 = 1.0000, 임금이 없으면 null)
		private final BigDecimal wageRatio;

		// 최신 전세(거래유형 J) 가격 (데이터가 없으면 null)
		private final HousingPriceResponse housing;

		// 기준 대비 전세 비율 (기준 지역 = 1.0000, 전세 데이터가 없으면 null)
		private final BigDecimal housingRatio;
	}
}
