package com.lifescope.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
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
	// 캐시 계층은 Optional 을 언래핑하므로 Optional 을 반환하면 캐시 히트 시
	// 캐스팅 불일치(ClassCastException)가 발생한다. null 로 반환하고 unless 로 제어한다.
	@Cacheable(value = "wageLatest", key = "#cityCode", unless = "#result == null")
	public WageResponse getLatestWage(String cityCode){
		return wageRepository.findTopByCityCodeOrderByYearDesc(cityCode)
				.map(WageResponse::from)
				.orElse(null);
	}
	
	// 특정 도시의 특정 연도 임금 조회
	@Cacheable(value = "wageByYear", key = "#cityCode + ':' + #year", unless = "#result == null")
	public WageResponse getWageByYear(String cityCode, Short year){
		return wageRepository.findByCityCodeAndYear(cityCode, year)
				.map(WageResponse::from)
				.orElse(null);
	}
	
	// 특정 연도 전체 도시 임금 순위 (전국 비교용)
	@Cacheable(value = "wageRanking", key = "#year")
	public List<WageResponse> getWagesByYear(Short year){
		return wageRepository.findAllByYearOrderByWageAvgDesc(year)
				.stream()
				.map(WageResponse::from)
				.toList();
	}
}
