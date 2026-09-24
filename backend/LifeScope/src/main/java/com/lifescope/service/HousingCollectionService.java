package com.lifescope.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.lifescope.client.MolitClient;
import com.lifescope.client.dto.RentApiItem;
import com.lifescope.client.dto.TradeApiItem;
import com.lifescope.domain.city.City;
import com.lifescope.domain.city.CityRepository;
import com.lifescope.domain.housing.HousingPrice;
import com.lifescope.domain.housing.HousingRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 주거비 (실거래가) 수집 -> 도시별 평균 집계 -> DB 저장 서비스
//          - M (매매) : 매매가 평균 (만원)
//          - J (전세) : 전세보증금 평균 (만원)
//          - W (월세) : 월세금액 평균 (만원)
@Slf4j
@Service
@RequiredArgsConstructor
public class HousingCollectionService {

    private final MolitClient molitClient;
    private final HousingRepository housingRepository;
    private final CityRepository cityRepository;

    // 전달 월치 수집
    @Transactional
    @CacheEvict(value = {"housingLatest", "housingHistory", "comparison", "comparisonMulti"}, allEntries = true)
    public int collectMonthlyHousing(String dealYmd){
        List<City>  cities = cityRepository.findByLevelAndIsActiveTrue((short) 1);              // 17개 시/도 (LAWD_CD 와 동일)

        int saved = 0;
        int updated = 0;
        int failed = 0;

        for(City city : cities){
            try{
                // 1. 매매 -> M
                int[] sale = aggregateSale(molitClient.fetchAptSale(city.getCode(), dealYmd));
                int r = upsert(city, "M", dealYmd, sale);
                saved += r;
                updated += (sale != null && r == 0 ? 1 : 0);

                // 2. 전월세 -> J (보증금) / W (월세)
                List<RentApiItem> rents = molitClient.fetchAptRent(city.getCode(), dealYmd);
                int[] jeonse = aggregateRent(rents, true);
                r = upsert(city, "J", dealYmd, jeonse);
                saved += r;
                updated += (jeonse != null && r == 0 ? 1 : 0);
                
                int [] wolse = aggregateRent(rents, false);
                r = upsert(city, "W", dealYmd, wolse);
                saved += r;
                updated += (wolse != null && r == 0 ? 1 : 0);
            } catch (Exception e) {
            	// 도시 1개 실패가 전체 수집 중단시키지 않도록 예외 격리
            	log.warn("주거비 수집 실패, 스킵 : city = {}, ym = {}, 원인 = {}", city.getName(), dealYmd, e.getMessage());
            	failed++;
            }
        }
        log.info("주거비 수집 완료 ({}) : 신규 {} 건, 갱신 {} 건, 실패 지역 {} 개", dealYmd, saved, updated, failed);
        return saved + updated;
    }
    
    // 매매 평균가 집계 (만원) -> [평균가, 건수], 데이터 없으면 null
    private int[] aggregateSale(List<TradeApiItem> items) {
    	if(items == null || items.isEmpty()) return null;
    	long sum = 0;
    	int count = 0;
    	for(TradeApiItem item : items) {
    		Long v = parseManwon(item.getDealAmount());
    		if(v != null) {sum += v; count++;}
    	}
    	if(count == 0) return null;
    	return new int[] { (int) Math.round((double) sum / count), count };
    }
    
    // 전월세 평균 집계 - isJeonse = true 면 보증금(J), false 면 월세 금액 (W)
    private int[] aggregateRent(List<RentApiItem> items, boolean isJeonse) {
    	if(items == null || items.isEmpty()) return null;
    	long sum = 0;
    	int count = 0;
    	for(RentApiItem item : items) {
    		Long v = isJeonse
    				? parseManwon(item.getDeposit())
    						: parseManwon(item.getMonthlyRent());
    		if(v != null && v > 0) {sum += v; count++;}
    	}
    	if(count == 0) return null;
    	return new int[] { (int) Math.round((double) sum / count), count };
    }
    
    // "85,000" -> 85000 (만원) 변환 - null / 공백 / "-" / 0 방어
    private Long parseManwon(String raw) {
    	if(raw == null) return null;
    	String trimmed = raw.trim().replace(",", "");
    	if(trimmed.isEmpty() || "-".equals(trimmed)) return null;
    	try {
    		return Long.parseLong(trimmed);
    	} catch (NumberFormatException e) {
    		return null;
    	}
    }
    
    // 도시 + 거래 유형 + 연월 upsert - 반환 1 = 신규, 0 = 갱신 또는 스킵
    private int upsert(City city, String tradeType, String dealYmd, int[] aggregated) {
    	if(aggregated == null) {
    		log.debug("집계 데이터 없음, 스킵 : city = {}, type = {}, ym = {}", city.getName(), tradeType, dealYmd);
    		return 0;
    	}
    	long avgPrice = aggregated[0];
    	int dealCount = aggregated[1];
    	
    	HousingPrice existing = housingRepository
    			.findByCityCodeAndTradeTypeAndYearMonth(city.getCode(), tradeType, dealYmd);
    	if(existing != null) {
    		existing.setAvgPrice(avgPrice);
    		existing.setDealCount(dealCount);
    		return 0;			// 갱신
    	}
    	HousingPrice price = new HousingPrice();
    	price.setCity(city);
    	price.setTradeType(tradeType);
    	price.setYearMonth(dealYmd);
    	price.setAvgPrice(avgPrice);
    	price.setDealCount(dealCount);
    	housingRepository.save(price);
    	return 1;				// 신규
    }
}
