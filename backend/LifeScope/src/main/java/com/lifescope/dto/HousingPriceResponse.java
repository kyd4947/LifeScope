package com.lifescope.dto;

import com.lifescope.domain.housing.HousingPrice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 주거비 실거래가 응답 DTO
@Getter
@Builder
@AllArgsConstructor
public class HousingPriceResponse {

	// 지역 코드
	private final String cityCode;
	
	// 지역명
	private final String cityName;
	
	// 거래 유형 코드 : M - 매매 / J - 전세 / W - 월세
	private final String tradeType;
	
	// 거래 유형 한글
	private final String tradeTypeLabel;
	
	// 연/월
	private final String yearMonth;
	
	// 평균가 (만 원)
	private final Long avgPrice;
	
	// 거래 건수
	private final Integer dealCount;
	
	// 엔티티 -> DTO 변환
	public static HousingPriceResponse from(HousingPrice price) {
		return HousingPriceResponse.builder()
				.cityCode(price.getCity().getCode())
				.cityName(price.getCity().getName())
				.tradeType(price.getTradeType())
				.tradeTypeLabel(price.getTradeTypeLabel())
				.yearMonth(price.getYearMonth())
				.avgPrice(price.getAvgPrice())
				.dealCount(price.getDealCount())
				.build();
	}
}
