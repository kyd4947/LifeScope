package com.lifescope.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.housing.HousingRepository;
import com.lifescope.dto.HousingPriceResponse;
import com.lifescope.exception.InvalidTradeTypeException;

import lombok.RequiredArgsConstructor;

// 주거비 실거래가 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class HousingService {

	// 유효한 거래 유형
	private static final Set<String> VALID_TRADE_TYPES = Set.of("M", "J", "W");
	
	private final HousingRepository housingRepository;
	
	// 특정 도시의 최신 주거비 조회
	// 캐시 계층은 Optional 을 언래핑하므로 Optional 을 반환하면 캐시 히트 시
	// 캐스팅 불일치(ClassCastException)가 발생한다. null 로 반환하고 unless 로 제어한다.
	@Cacheable(value = "housingLatest", key = "#cityCode + ':' + #tradeType", unless = "#result == null")
	public HousingPriceResponse getLatestPrice(String cityCode, String tradeType){
		validateTradeType(tradeType);
		return housingRepository
				.findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(cityCode, tradeType)
				.map(HousingPriceResponse::from)
				.orElse(null);
	}
	
	// 특정 도시의 기간별 주거비 이력 조회 (from/to : YYYYMM)
	@Cacheable(value = "housingHistory", key = "#cityCode + ':' + #tradeType + ':' + #from + ':' + #to")
	public List<HousingPriceResponse> getPriceHistory(String cityCode, String tradeType, String from, String to){
		validateTradeType(tradeType);
		return housingRepository
				.findByCityCodeAndTradeTypeAndYearMonthBetween(cityCode, tradeType, from, to)
				.stream()
				.map(HousingPriceResponse::from)
				.toList();
	}
	
	// 거래 유형 검증 (M/J/W 외 값 차단)
	private void validateTradeType(String tradeType) {
		if(!VALID_TRADE_TYPES.contains(tradeType)) {
			throw new InvalidTradeTypeException("유효하지 않은 거래 유형 : " + tradeType + " (M = 매매, J = 전세, W = 월세)");
		}
	}
}
