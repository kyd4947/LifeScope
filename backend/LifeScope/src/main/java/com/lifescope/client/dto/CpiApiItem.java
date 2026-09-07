package com.lifescope.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// KOSIS CPI 응답 1건 매핑용 DTO (DB 엔티티와 분리)
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class CpiApiItem {

	// 수치값
	@JsonProperty("DT")
	private String value;
	
	// 수록 시점 - year_month에 그대로 사용
	@JsonProperty("PRE_DE")
	private String period;
	
	// 지역 한글 명 - 코드 변환 키
	@JsonProperty("C1_NM")
	private String regionName;
	
	// 단위
	@JsonProperty("UNIT_NM")
	private String unit;
	
	// 항목명 - 총지수 검증용
	@JsonProperty("ITM_NM")
	private String itemName;
}
