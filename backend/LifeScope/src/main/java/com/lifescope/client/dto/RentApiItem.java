package com.lifescope.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 국토부 아파트 전월세 실거래 1건 매핑용 DTO
//		- 전세 : 보증금액에 값, 월세금액 0
//		- 월세 : 보증금액 + 월세금액 모두 값
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)			// 아파트명·면적 등 불필요 필드 무시
public class RentApiItem {

	// 보증금 (만원, 콤마 포함 문자열)
	@JsonProperty("deposit")
	private String deposit;
	
	// 월세 (만원, 콤마 포함 문자열)
	@JsonProperty("monthlyRent")
	private String monthlyRent;
}
