package com.lifescope.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.ComparisonResult;
import com.lifescope.dto.MultiComparisonResponse;
import com.lifescope.service.ComparisonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 지역 간 생활비 비교 API (서비스 핵심 기능) - 월급의 물가 보정 환산 + 임금/주거비 비교
@RestController
@RequiredArgsConstructor
public class ComparisonController {

	// 다중 비교 시 허용할 최소/최대 지역 수
	private static final int MIN_CITIES = 2;
	private static final int MAX_CITIES = 5;
	
	private final ComparisonService comparisonService;
	
	@Operation(summary="지역 간 생활비 비교", description="두 지역 생활비 종합 비교 API")
	@GetMapping("/api/comparison")
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
	
	@Operation(summary="다중 지역 생활비 비교", description="2~5개 지역 생활비 종합 비교 API (첫 번째 지역이 기준)")
	@GetMapping("/api/compare")
	public ResponseEntity<MultiComparisonResponse> compareMulti(
			@Parameter(description="비교할 지역 코드 (콤마 구분, 2~5개, 첫 번째가 기준)") @RequestParam String cities,
			@Parameter(description="기준 지역 월급") @RequestParam Long salary){
		
		// 월급 0 이하 차단 (GlobalExceptionHandler가 400으로 변환)
		if(salary == null || salary <= 0) {
			throw new IllegalArgumentException("월급은 0보다 큰 값이어야 합니다.");
		}
		
		// 공백 제거 + 빈 값 제거 + 중복 제거 (순서 유지 -> 첫 번째 도시가 기준)
		List<String> codes = Arrays.stream(cities.split(","))
				.map(String::trim)
				.filter(code -> !code.isEmpty())
				.distinct()
				.toList();
		
		if(codes.size() < MIN_CITIES || codes.size() > MAX_CITIES) {
			throw new IllegalArgumentException("비교할 지역은 " + MIN_CITIES + "~" + MAX_CITIES + "개여야 합니다.");
		}
		
		return ResponseEntity.ok(comparisonService.compareMulti(codes, salary));
	}
}
