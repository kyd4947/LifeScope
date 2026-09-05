package com.lifescope.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.CpiResponse;
import com.lifescope.exception.DataNotFoundException;
import com.lifescope.service.CostOfLivingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 소비자 물가 지수(CPI) API - 최신 CPI 또는 기간별 이력 조회 제공
@RestController
@RequestMapping("/api/cities/{code}/cpi")
@RequiredArgsConstructor
public class CpiController {

	private final CostOfLivingService costOfLivingService;
	
	// CPI 조회 - from/to 있으면 이력, 없으면 최신 1건
	@Operation(summary="물가 지수 조회", description="최신 CPI 1건 또는 기간별 이력 반환")
	@GetMapping
	public ResponseEntity<?> getCpi(
			@Parameter(description="지역 코드") @PathVariable String code,
			@Parameter(description="시작 연월 (YYYYMM), 생략 시 최신") @RequestParam(required=false) String from,
			@Parameter(description="종료 연월 (YYYYMM), 생략 시 최신") @RequestParam(required=false) String to){
		
		// from/to 둘 중 하나만 오면 잘못도니 요청
		if((from == null) != (to == null)) {
			throw new IllegalArgumentException("시작과 종료는 함께 지정해야 합니다.");
		}
		if(from != null) {
			List<CpiResponse> history = costOfLivingService.getCpiHistory(code, from, to);
			return ResponseEntity.ok(history);
		}
		return ResponseEntity.ok(costOfLivingService.getLatestCpi(code)
				.orElseThrow(() -> new DataNotFoundException("물가 지수 데이터가 없습니다. 지역 코드 : " + code)));
	}
}
