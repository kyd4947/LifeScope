package com.lifescope.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.city.City;
import com.lifescope.domain.city.CityRepository;
import com.lifescope.dto.CityResponse;
import com.lifescope.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;

// 지역 조회 서비스 : 시/도, 시/군/구 계층 조회 및 검색 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {

	private final CityRepository cityRepository;
	
	// 전체 시/도 (level=1) 목록 조회
	public List<CityResponse> getAllCity(){
		return cityRepository.findByLevelAndIsActiveTrue((short) 1)
				.stream()
				.map(CityResponse::from)
				.toList();
	}
	
	// 특정 시/도에 속한 시/군/구 (level=2) 목록 조회
	public List<CityResponse> getTownByParent(String parentCode){
		return cityRepository.findByParentCodeAndIsActiveTrue(parentCode)
				.stream()
				.map(CityResponse::from)
				.toList();
	}
	
	// 지역명 키워드 검색
	public List<CityResponse> searchByName(String keyword){
		return cityRepository.findByNameContainingAndIsActiveTrue(keyword)
				.stream()
				.map(CityResponse::from)
				.toList();
	}
	
	// 활성 지역 단건 조회 (없으면 예외)
	public CityResponse getActiveCity(String code) {
		return cityRepository.findByCodeAndIsActiveTrue(code)
				.map(CityResponse::from)
				.orElseThrow(() -> new DataNotFoundException("존재하지 않는 지역 코드 : " + code));
	}
	
	// 지역 엔티티 조회 - ComparisonService 등 내부 서비스 간 검증용
	public City getCityEntity(String code) {
		return cityRepository.findByCodeAndIsActiveTrue(code)
				.orElseThrow(() -> new DataNotFoundException("존재하지 않는 지역 코드 : " + code));
	}
}
