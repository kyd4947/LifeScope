package com.lifescope.domain.housing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HousingRepository extends JpaRepository<HousingPrice, Long>{
	// 해당 도시 특정 거래 유형 최신 데이터 조회
	Optional<HousingPrice> findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(
			String cityCode, String tradeType);
	
	// 해당 도시, 거래 유형, 기간 내 실거래가 목록 조회
	List<HousingPrice> findByCityCodeAndTradeTypeAndYearMonthBetween(
			String cityCode, String tradeType, String start, String end);
	
	// upsert 용 : 도시·거래유형·연월 정확히 일치하는 1건 조회
	HousingPrice findByCityCodeAndTradeTypeAndYearMonth(String cityCode, String tradeType, String yearMonth);
}
