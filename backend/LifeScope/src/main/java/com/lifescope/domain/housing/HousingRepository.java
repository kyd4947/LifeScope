package com.lifescope.domain.housing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HousingRepository extends JpaRepository<HousingPrice, Long>{
	// 해당 도시 특정 거래 유형 최신 데이터 조회
	Optional<HousingPrice> findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(
			String cityCode, String tradeType);
	
	// 해당 도시, 거래 유형, 기간 내 실거래가 목록 조회
	List<HousingPrice> findByCityCodeAndTradeTypeAndYearMonthBetween(
			String cityCode, String tradeType, String start, String end);
	
	// 해당 도시 특정 거래 유형 최근 N건 조회
	@Query("SELECT h FROM HousingPrice h WHERE h.city.code = :cityCode AND h.tradeType = :tradeType ORDER BY h.yearMonth DESC")
	List<HousingPrice> findRecentByCityCodeAndTradetype(@Param("cityCode") String cityCode,
			@Param("tradeType") String tradeType, @Param("limit") int limit);
	
	// 특정 도시 특정 거래 유형 평균가 조회 (since 이후)
	@Query("SELECT AVG(h.avgPrice) FROM HousingPrice h WHERE h.city.code = :cityCode AND h.tradeType = :tradeType AND h.yearMonth >= :since")
	Optional<Long> findAveragePriceSince(@Param("cityCode") String cityCode, 
			@Param("tradeType") String tradeType, @Param("since") String since);
	
	// 특정 도시 매매 평균가 목록 (최신순)
	@Query("SELECT h FROM HousingPrice h WHERE h.city.code = :cityCode AND h.tradeType = 'M' ORDER BY h.yearMonth DESC")
	List<HousingPrice> findMaptPricesByCityCode(@Param("cityCode") String cityCode);
}
