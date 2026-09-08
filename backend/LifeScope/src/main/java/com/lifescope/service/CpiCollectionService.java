package com.lifescope.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.client.KosisClient;
import com.lifescope.client.KosisRegionMapper;
import com.lifescope.client.dto.CpiApiItem;
import com.lifescope.domain.city.City;
import com.lifescope.domain.city.CityRepository;
import com.lifescope.domain.costOfLiving.ConsumerPriceIndex;
import com.lifescope.domain.costOfLiving.CpiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// CPI 수집 -> DB 저장 서비스 (쓰기 작업이라 readOnly 아님) - 존재하면 값 갱신, 없으면 신규 저장
@Slf4j
@Service
@RequiredArgsConstructor
public class CpiCollectionService {

	// UNIT_NM (e.g. : "2020 = 100", = 는 전각)에서 연도 4 자리 추출
	private static final Pattern BASE_YEAR_PATTERN = Pattern.compile("(\\d{4})");
	
	private final KosisClient kosisClient;
	private final CpiRepository cpiRepository;
	private final CityRepository cityRepository;
	
	// 최신(월) CPI 수집 1회 실행, 저장 + 갱신 건수 변환
	@Transactional
	public int collecLatestCpi() {
		List<CpiApiItem> items = kosisClient.fetchLatestCpi();
		
		int saved = 0;
		int updated = 0;
		int skipped = 0;
		
		for(CpiApiItem item : items) {
			// 1. 총 지수 항목만 처리 (itmId 방어 검증)
			if(item.getItemName() == null || !item.getItemName().contains("총지수")) {
				log.warn("총 지수 아님, 스킵 : itemName = {}", item.getItemName());
				skipped++;
				continue;
			}
			
			// 2. 지역명 -> 코드 (전국 등은 empty)
			Optional<String> cityCode = KosisRegionMapper.toCityCode(item.getRegionName());
			if(cityCode.isEmpty()) {
				log.debug("저장 제외 지역, 스킵 : regionName = {}", item.getRegionName());
				skipped++;
				continue;
			}
			
			// 3. City 조회 (V4 적용 후 전북 포함 17개 존재해야 정상)
			City city = cityRepository.findByCodeAndIsActiveTrue(cityCode.get()).orElse(null);
			if(city == null) {
				log.warn("city 테이블에 없음, 스킵 : code = {}", cityCode.get());
				skipped++;
				continue;
			}
			
			// 4. 값 변환 (기준 년도 추출 + 수치 파싱)
			Short baseYear = parseBaseYear(item.getUnit());
			String yearMonth = item.getPeriod();
			BigDecimal value = new BigDecimal(item.getValue().trim());
			
			// 5. upsert
			Optional<ConsumerPriceIndex> existing =
					cpiRepository.findByCityCodeAndBaseYearAndYearMonth(cityCode.get(), baseYear, yearMonth);
			if (existing.isPresent()) {
				existing.get().setCpiValue(value);
				updated++;
			} else {
				ConsumerPriceIndex cpi = new ConsumerPriceIndex();
				cpi.setCity(city);
				cpi.setBaseYear(baseYear);
				cpi.setYearMonth(yearMonth);
				cpi.setCpiValue(value);
				cpiRepository.save(cpi);
				saved++;
			}
		}
		
		log.info("CPI 수집 완료 : 신규 {} 건, 갱신 {} 건, 스킵 {} 건", saved, updated, skipped);
		return saved + updated;
	}
	
	// "2020 = 100" 에서 앞 4자리 연도 추출 (전각 = 무관, 숫자 패턴 기준)
	private Short parseBaseYear(String unit) {
		if(unit != null) {
			Matcher matcher = BASE_YEAR_PATTERN.matcher(unit);
			if(matcher.find()) {
				return Short.parseShort(matcher.group(1));
			}
		}
		throw new IllegalStateException("기준 년도 파싱 실패 : unit = " + unit);
	}
}
