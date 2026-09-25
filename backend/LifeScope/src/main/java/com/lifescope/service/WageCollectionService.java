package com.lifescope.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.client.KosisClient;
import com.lifescope.client.KosisRegionMapper;
import com.lifescope.client.dto.WageApiItem;
import com.lifescope.domain.city.City;
import com.lifescope.domain.city.CityRepository;
import com.lifescope.domain.wage.AverageWage;
import com.lifescope.domain.wage.WageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 평균 임금 수집 -> DB 저장 서비스 - KOSIS 연간 임금 (시/도 단위)
//				- wageMonthly 는 DB GENERATED ALWAYS 가 자동 계산하므로 값 지정 금지
@Slf4j
@Service
@RequiredArgsConstructor
public class WageCollectionService {

	private final KosisClient kosisClient;
	private final WageRepository wageRepository;
	private final CityRepository cityRepository;
	
	@Value("${kosis.wage-org-id:}")
	private String wageOrgId;
	
	@Value("${kosis.wage-tbl-id:}")
	private String wageTblId;
	
	@Value("${kosis.wage-itm-id:}")
	private String wageItmId;
	
	// 최신 연간 임금 수집 (전 지역)
	@Transactional
	@CacheEvict(value = {"wageLatest", "wageByYear", "wageRanking", "comparison", "comparisonMulti"}, allEntries = true)
	public int collectLatestWage() {
		List<WageApiItem> items = kosisClient.fetchLatestWage(wageOrgId, wageTblId, wageItmId);
		if(items == null || items.isEmpty()) {
			throw new IllegalStateException("KOSIS 임금 응답이 0 건 입니다. 임금 통계표 ID(kosis.wage-*) 확인 필요");
		}
		
		int saved = 0;
		int updated = 0;
		int skipped = 0;
		
		for(WageApiItem item : items) {
			// 0. 항목 필터 : itmId=ALL 로 조회하면 "상용 월평균 임금" 과
			//    "상용 월평균 임금 전년 대비 증감률" 이 함께 내려온다.
			//    증감률은 단위가 % 이고 값이 2.7 처럼 소수라 임금으로 쓰면 안 된다.
			if(item.getItemName() == null || !item.getItemName().contains("월평균 임금")) {
				skipped++;
				continue;
			}
			// 증감률 항목은 이름에 "증감률" 이 붙는다. 임금만 남긴다.
			if(item.getItemName().contains("증감률")) {
				skipped++;
				continue;
			}
			
			// 1. 지역명 -> 코드 (전국 등은 empty)
			Optional<String> cityCode = KosisRegionMapper.toCityCode(item.getRegionName());
			if(cityCode.isEmpty()) {
				skipped++;
				continue;
			}
			
			// 2. City 검증
			City city = cityRepository.findByCodeAndIsActiveTrue(cityCode.get()).orElse(null);
			if(city == null) {
				log.warn("city 테이블에 없음, 스킵 : code = {}", cityCode.get());
				skipped++;
				continue;
			}
			
			// 3. 값 변환 (연도 + 단위 환산)
			Short year;
			Long wageWon;
			try {
				year = Short.parseShort(item.getPeriod().trim());
				wageWon = parseToWon(item.getValue(), item.getUnit());
			} catch (RuntimeException e) {
				log.warn("임금 값 파싱 실패, 스킵 : region = {}, value = {}, unit = {}", item.getRegionName(), item.getValue(), item.getUnit());
				skipped++;
				continue;
			}
			if(wageWon == null) {
				skipped++;
				continue;
			}
			
			// 4. upsert (city + year)
			Optional<AverageWage> existing = wageRepository.findByCityCodeAndYear(cityCode.get(), year);
			if(existing.isPresent()) {
				existing.get().setWageAvg(wageWon);
				updated++;
			} else {
				AverageWage wage = new AverageWage();
				wage.setCity(city);
				wage.setYear(year);
				wage.setWageAvg(wageWon);
				wageRepository.save(wage);
				saved++;
			}
		}
		log.info("임금 수집 완료 : 신규 {} 건, 갱신 {} 건, 스킵 {} 건", saved, updated, skipped);
		return saved + updated;
	}
	
	// 값 + 단위 -> 원 환산
	private Long parseToWon(String raw, String unit) {
		if(raw == null) return null;
		String trimmed = raw.trim().replace(",", "");
		if(trimmed.isEmpty() || "-".equals(trimmed)) return null;
		long value;
		try {
			value = Long.parseLong(trimmed);
		}catch (NumberFormatException e) {
			return null;
		}
		if(unit == null) return value;
		if(unit.contains("천원")) return value * 1_000L;
		if(unit.contains("백만원")) return value * 1_000_000L;
		return value;			// "원" 이거나 미상 단위
	}
}
