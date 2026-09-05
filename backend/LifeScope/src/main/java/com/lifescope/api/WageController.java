package com.lifescope.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.WageResponse;
import com.lifescope.exception.DataNotFoundException;
import com.lifescope.service.WageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 평균 임금 API -  특정 지역 임금 조회 + 전국 임금 순위 제공
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WageController {
	
	private final WageService wageService;

	// 지역별 임금 조회 - year 있으면 해당 연도, 없으면 최신
	@Operation(summary="지역 임금 조회", description="특정 지역의 최신 또는 특정 연도 임금 반환")
	@GetMapping("/cities/{code}/wage")
	public ResponseEntity<WageResponse> getWage(
			@Parameter(description="지역 코드") @PathVariable String code,
			@Parameter(description="조회 연도, 생략 시 최신") @RequestParam(required=false) Short year){
		
		return ResponseEntity.ok(
				(year != null ? wageService.getWageByYear(code, year) : wageService.getLatestWage(code))
				.orElseThrow(() -> new DataNotFoundException("임금 데이터가 없습니다. 지역 코드 : " + code)));
	}
	
	// 전국 임금 순위
	@Operation(summary="전국 임금 순위", description="특정 연도 전체 지역 임금 내림차순 반환")
	@GetMapping("/wages")
	public ResponseEntity<List<WageResponse>> getWageRanking(
			@Parameter(description="조회 연도") @RequestParam Short year){
		return ResponseEntity.ok(wageService.getWagesByYear(year));
	}
}
