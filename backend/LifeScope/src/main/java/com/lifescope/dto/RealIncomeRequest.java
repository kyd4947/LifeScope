package com.lifescope.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 실수령액 계산 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealIncomeRequest {

	// 연봉 (원)
	@NotNull(message = "연봉은 필수")
	@Min(value = 0, message = "연봉은 0 이상이어야 합니다.")
	@Max(value = 2_000_000_000L, message = "연봉이 비정상적으로 큽니다.")
	private Long annualSalary;
}
