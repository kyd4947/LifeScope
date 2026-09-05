package com.lifescope.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.CityResponse;
import com.lifescope.service.CityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
	
	// 지역 코드 단건 조회
	@Operation(summary="지역 단건 조회", description="코드로 활성 지역 1개 조회")
	@GetMapping("/{code}")
	public ResponseEntity<CityResponse> getCity(@Parameter(description="지역코드") @PathVariable String code){
		return ResponseEntity.ok(cityService.getActiveCity(code));
	}
	
	// 특정 시/도 하위 시/군/구 목록
	@Operation(summary="시/군/구 목록 조회", description="특정 시/도에 속한 시/군/구 목록 반환")
	@GetMapping("/{code}/towns")
	public ResponseEntity<List<CityResponse>> getTowns(@Parameter(description="상위 시/도 코드") @PathVariable String code){
		return ResponseEntity.ok(cityService.getTownByParent(code));
	}
	
	// 지역명 검색
	@Operation(summary="지역명 검색", description="이름에 키워드가 포함된 지역 목록 반환")
	@GetMapping("/search")
	public ResponseEntity<List<CityResponse>> searchByName(@Parameter(description= "검색 키워드") @RequestParam String name){
		return ResponseEntity.ok(cityService.searchByName(name));
	}
}
