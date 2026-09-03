package com.lifescope.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.wage.WageRepository;
import com.lifescope.dto.WageResponse;

import lombok.RequiredArgsConstructor;

// 평균 임금 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class WageService {

	private final WageRepository wageRepository;
	
	// 특정 도시의 최신 임금 조회
	public Optional<WageResponse> getLatestWage(String cityCode){
		return wageRepository.findTopByCityCodeOrderByYearDesc(cityCode)
				.map(WageResponse::from);
	}
	
	// 특정 도시의 특정 연도 임금 조회
	public Optional<WageResponse> getWageByYear(String cityCode, Short year){
		return wageRepository.findByCityCodeAndYear(cityCode, year)
				.map(WageResponse::from);
	}
	
	// 특정 연도 전체 도시 임금 순위 (전국 비교용)
	public List<WageResponse> getWagesByYear(Short year){
		return wageRepository.findAllByYearOrderByWageAvgDesc(year)
				.stream()
				.map(WageResponse::from)
				.toList();
	}
}
