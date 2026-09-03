package com.lifescope.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.costOfLiving.CpiRepository;
import com.lifescope.dto.CpiResponse;

import lombok.RequiredArgsConstructor;

// 소비자 물가 지수 (CPI) 서비스 - 지역별 CPI 조회 및 평균 집계 담당
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class CostOfLivingService {

	private final CpiRepository cpiRepository;
	
	// 특정 도시의 최신 CPI 조회
	public Optional<CpiResponse> getLatestCpi(String cityCode){
		return cpiRepository.findTopByCityCodeOrderByYearMonthDesc(cityCode)
				.map(CpiResponse::from);
	}
	
	// 특정 도시의 기간별 CPI 이력 조회 (from/to : YYYYMM)
	public List<CpiResponse> getCpiHistory(String cityCode, String from, String to){
		return cpiRepository.findByCityCodeAndYearMonthBetween(cityCode, from, to)
				.stream()
				.map(CpiResponse::from)
				.toList();
	}
	
	// 특정 도시의 특정 시점 이후 CPI 평균값 조회
	public Optional<BigDecimal> getAverageCpiSince(String cityCode, String since){
		return cpiRepository.findAverageCpiSince(cityCode, since);
	}
}
