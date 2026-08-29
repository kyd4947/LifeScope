package com.lifescope.domain.costOfLiving;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CpiRepository extends JpaRepository<ConsumerPriceIndex, Long>{
	
	// 해당 도시의 최신 CPI 조회
	Optional<ConsumerPriceIndex> findTopByCityCodeOrderByYearMonthDesc(String cityCode);
	
	// 해당 도시, 기간 내 CPI 목록 조회
	List<ConsumerPriceIndex> findByCityCodeAndYearMonthBetween(
			String cityCode, String startYearMonth, String endYearMonth);
	
	// 해당 도시 특정 년도 최신 CPI 조회
	Optional<ConsumerPriceIndex> findTopByCityCodeAndBaseYearOrderByYearMonthDesc(String cityCode, Short baseYear);
	
	// 특정 도시 최근 N건 CPI 조회
	@Query("SELECT c FROM ConsumerPriceIndex c WHERE c.city.code = :cityCode ORDER BY c.yearMonth DESC")
	List<ConsumerPriceIndex> findRecentByCityCode(@Param("cityCode") String cityCode, @Param("limit") int limit);
	
	// 특정 도시 최근 CPI 평균값 조회
	@Query("SELECT AVG(c.cpiValue) FROM ConsumerPriceIndex c WHERE c.city.code = :cityCode AND c.yearMonth >= :since")
	Optional<BigDecimal> findAverageCpiSince(@Param("cityCode") String cityCode, @Param("since") String since);
}
