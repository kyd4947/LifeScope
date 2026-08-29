package com.lifescope.domain.city;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 도시 정보 관리 위한 Spring Data JPA Repository 인터페이스
 * 활성화된 지역 (isActive = true) 지역 데이터를 기준으로 조회하는 메서드 제공
 */
@Repository
public interface CityRepository extends JpaRepository<City, String>{
	// 상위 지역 코드 받아 하위 활성 도시 목록 조회
	List<City> findByParentCodeAndIsActiveTrue(String parentCode);
	
	// 검색 키워드 포함된 활성 도시 목록 조회
	List<City> findByNameContainingAndIsActiveTrue(String keyword);
	
	// 지역 코드 활성 상태인 단 건 도시 정보 조회
	Optional<City> findByCodeAndIsActiveTrue(String code);
	
	// 모든 활성 도시 지역 계층 순, 이름 순으로 정렬 및 조회
	@Query("SELECT c FROM City c WHERE c.isActive = true ORDER BY c.level, c.name")
	List<City> findAllActiveOrderByLevelAndName();
	
	// 특정 계층 레벨 해당 활성 도시 목록 이름 순으로 조회
	@Query("SELECT c FROM City c WHERE c.level = :level AND c.isActive = true ORDER BY c.name")
	List<City> findByLevelAndIsActiveTrue(@Param("level") Short level);
}
