package com.lifescope.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 실수령액 계산 응답 DTO - 공제 내역 상세 포함
@Getter
@Builder
@AllArgsConstructor
public class RealIncomeResponse {

	// 입력 연봉 (원)
	private final Long annualSalary;
	
	// 월 세전 금액 (원)
	private final Long monthlyGross;
	
	// 국민 연금 (월, 원)
	private final Long pension;
	
	// 건강 보험 (월, 원)
	private final Long health;
	
	// 장기 요양 보험 (월, 원) - 건강보험료의 12.95%
	private final Long care;
	
	// 고용 보험 (월, 원)
	private final Long employment;
	
	// 소득세 (월, 원) - 간이 근사치
	private final Long incomeTax;
	
	// 월 실수령액 (원)
	private final Long monthlyNet;
	
	// 월 총 공제액 (원)
	private final Long totalDeduction;
}
