package com.lifescope.domain.wage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WageRepository extends JpaRepository<AverageWage, Long> {
	// 해당 도시 최신 임금 데이터 조회
	Optional<AverageWage> findTopByCityCodeOrderByYearDesc(String cityCode);
	
	// 해당 도시 특정 연도 임금 조회
	Optional<AverageWage> findByCityCodeAndYear(String cityCode, Short year);
	
	// 특정 도시 최신 N건 임금 조회
	@Query("SELECT w FROM AverageWage w WHERE w.city.code = :cityCode ORDER BY w.year DESC")
	List<AverageWage> findRecentByCityCode(@Param("cityCode") String cityCode, @Param("limit") int limit);
	
	// 모든 도시 특정 연도 임금 목록 (전국 비교용)
	@Query("SELECT w FROM AverageWage w WHERE w.year = :year ORDER BY w.wageAvg DESC")
	List<AverageWage> findAllByYearOrderByWageAvgDesc(@Param("year") Short year);
}
