package com.lifescope.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.CityResponse;
import com.lifescope.service.CityService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

// 지역 조회 API - 시/도 목록, 시/군/구 목록, 검색, 단건 조회 제공
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {
	
	private final CityService cityService;
	
	// 전체 시/도 (level=1) 목록
	@Operation(summary="시/도 목록 조회", description="전국 17개 시/도 목록 반환")
	@GetMapping
	public ResponseEntity<List<CityResponse>> getAllCity(){
		return ResponseEntity.ok(cityService.getAllCity());
	}
}
