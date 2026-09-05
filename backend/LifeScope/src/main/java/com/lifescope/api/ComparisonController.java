package com.lifescope.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.ComparisonResult;
import com.lifescope.service.ComparisonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 지역 간 생활비 비교 API (서비스 핵심 기능) - 월급의 물가 보정 환산 + 임금/주거비 비교
@RestController
@RequestMapping("/api/comparison")
@RequiredArgsConstructor
public class ComparisonController {

	private final ComparisonService comparisonService;
	
	@Operation(summary="지역 간 생활비 비교", description="두 지역 생활비 종합 비교 API")
	@GetMapping
	public ResponseEntity<ComparisonResult> compare(
			@Parameter(description="기준 지역 코드") @RequestParam String from,
			@Parameter(description="대상 지역 코드") @RequestParam String to,
			@Parameter(description="기준 지역 월급") @RequestParam Long salary){
		
		// 월급 0 이하 차단 (GlobalExceptionHandler가 400으로 변환)
		if(salary == null || salary <= 0) {
			throw new IllegalArgumentException("월급은 0보다 큰 값이어야 합니다.");
		}
		
		return ResponseEntity.ok(comparisonService.compare(from, to, salary));
	}
}
