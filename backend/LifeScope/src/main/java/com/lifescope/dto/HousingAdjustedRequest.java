package com.lifescope.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주거비 포함 비교 요청 DTO - 주거비 차이를 반영한 실질 구매력 계산
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingAdjustedRequest {

	// 기준 지역 (현재 거주지)
	@NotBlank(message = "기준 지역 코드는 필수")
	private String fromCode;
	
	// 대상 지역 (이주 가정 지역)
	@NotBlank(message = "대상 지역 코드는 필수")
	private String toCode;
	
	// 월급 (원)
	@NotNull(message = "월급은 필수")
	@Min(value = 1, message = "월급은 1원 이상이어야 합니다.")
	private Long monthlySalary;
	
	// 거래 유형 (기본값은 전세)
	@Pattern(regexp = "[MJW]", message = "거래 유형은 M, J, W 중 하나여야 합니다.")
	@Builder.Default
	private String tradeType = "J";
}
