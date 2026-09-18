package com.lifescope.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// KOSIS 임금 응답 1건 매핑용 DTO (CPI 응답과 구조 동일, 항목만 임금)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WageApiItem {

	// 수치값 (단위는 UNIT_NM 참조)
	@JsonProperty("DT")
	private String value;
	
	// 수록 시점 (연 단위)
	@JsonProperty("PRD_DE")
	private String period;
	
	// 지역 한글 명
	@JsonProperty("C1_NM")
	private String regionName;
	
	// 단위
	@JsonProperty("UNIT_NM")
	private String unit;
	
	// 항목명
	@JsonProperty("ITM_NM")
	private String itemName;
}
