package com.lifescope.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 물가 보정 환산 요청 DTO - A 지역 월급으로 B 지역 동등 생활 수준에 필요한 월급 계산
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostAdjustRequest {

	// 기준 지역 (현재 거주지)
	@NotBlank(message = "기준 지역 코드는 필수")
	private String fromCode;
	
	// 대상 지역 (이주 가정 지역)
	@NotBlank(message = "대상 지역 코드는 필수")
	private String toCode;
	
	// 월급 (원)
	@NotNull(message = "월급은 필수")
	@Min(value = 1, message = "월급은 1원 이상")
	private Long monthlySalary;
}
