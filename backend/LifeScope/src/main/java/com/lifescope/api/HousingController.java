package com.lifescope.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.HousingPriceResponse;
import com.lifescope.exception.DataNotFoundException;
import com.lifescope.service.HousingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

// 주거비 실거래가 API - tradeType 필수 (M=매매, J=전세, W=월세), 검증은 Service에서 수행
@RestController
@RequestMapping("/api/cities/{code}/housing")
@RequiredArgsConstructor
public class HousingController {

	private final HousingService housingService;
	
	// 주거비 조회 - from/to 있으면 이력, 없으면 최신 1건
	@Operation(summary="주거비 조회", description="최근 실거래가 1건 또는 기간별 이력 반환")
	@GetMapping
	public ResponseEntity<?> getHousingPrice(
			@Parameter(description="지역 코드") @PathVariable String code,
			@Parameter(description="거래 유형") @RequestParam String tradeType,
			@Parameter(description="시작 연월, 생략 시 최신") @RequestParam(required=false) String from,
			@Parameter(description="종료 연월, 생략 시 최신") @RequestParam(required=false) String to){
		if((from == null) != (to == null)) {
			throw new IllegalArgumentException("시작과 종료는 함께 지정해야 합니다.");
		}
		if(from != null) {
			List<HousingPriceResponse> history = housingService.getPriceHistory(code, tradeType, from, to);
			return ResponseEntity.ok(history);
		}
		return ResponseEntity.ok(housingService.getLatestPrice(code, tradeType)
				.orElseThrow(() -> new DataNotFoundException("주거비 데이터가 없습니다. 지역 코드 : " + code + ", 거래 유형 : " + tradeType)));
	}
}
