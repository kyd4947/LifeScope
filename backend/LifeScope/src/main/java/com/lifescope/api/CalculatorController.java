package com.lifescope.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.CostAdjustRequest;
import com.lifescope.dto.CostAdjustResponse;
import com.lifescope.dto.HousingAdjustedRequest;
import com.lifescope.dto.HousingAdjustedResponse;
import com.lifescope.dto.RealIncomeRequest;
import com.lifescope.dto.RealIncomeResponse;
import com.lifescope.service.CalculatorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 생활비 계산기 API - 실수령액 / 물가 보정 / 주거비 포함 실질 구매력
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class CalculatorController {

	private final CalculatorService calculatorService;
	
	// 실수령액 계산
	@Operation(summary = "실수령액 계산", description = "연봉 입력 -> 4대 보험 + 소득세 공제 후 월 실수령액 반환")
	@PostMapping("/real-income")
	public ResponseEntity<RealIncomeResponse> realIncome(
			@Parameter(description = "연봉 계산 요청") @Valid @RequestBody RealIncomeRequest request){
		return ResponseEntity.ok(calculatorService.realIncome(request.getAnnualSalary()));
	}
	
	// 물가 보정 환산
	@Operation(summary = "물가 보정 환산", description = "A 지역 월급으로 B 지역에서 동등한 생활 수준에 필요한 월급 반환")
	@PostMapping("/cost-adjust")
	public ResponseEntity<CostAdjustResponse> costAdjust(
			@Parameter(description = "물가 보정 요청") @Valid @RequestBody CostAdjustRequest request){
		return ResponseEntity.ok(calculatorService.costAdjust(request));
	}
	
	// 주거비 포함 실질 구매력
	@Operation(summary = "주거비 포함 비교", description = "주거비 차액을 반영한 실질 구매력 반환")
	@PostMapping("/housing-adjusted")
	public ResponseEntity<HousingAdjustedResponse> housingAdjusted(
			@Parameter(description = "주거비 포함 비교 요청") @Valid @RequestBody HousingAdjustedRequest request){
		return ResponseEntity.ok(calculatorService.housingAdjusted(request));
	}
}
