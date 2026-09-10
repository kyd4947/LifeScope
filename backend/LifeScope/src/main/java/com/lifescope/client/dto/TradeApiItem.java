package com.lifescope.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 국토부 아파트 매매 실거래 1건 매핑용 DTO
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)		// 도로명·지번 등 불필요 필드 무시
public class TradeApiItem {

	// 거래 금액 (만원, 콤마 포함 문자열)
	@JsonProperty("dealAmount")
	private String dealAmount;
	
	// 아파트명 (로그용)
	@JsonProperty("aptNm")
	private String aptName;
	
	// 법정동명 (로그용)
	@JsonProperty("umdNm")
	private String dongName;
}
