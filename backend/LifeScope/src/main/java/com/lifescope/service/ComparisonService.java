package com.lifescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.city.City;
import com.lifescope.domain.costOfLiving.ConsumerPriceIndex;
import com.lifescope.domain.costOfLiving.CpiRepository;
import com.lifescope.domain.housing.HousingPrice;
import com.lifescope.domain.housing.HousingRepository;
import com.lifescope.domain.wage.AverageWage;
import com.lifescope.domain.wage.WageRepository;
import com.lifescope.dto.CityResponse;
import com.lifescope.dto.ComparisonResult;
import com.lifescope.dto.ComparisonResult.HousingComparison;
import com.lifescope.dto.ComparisonResult.WageComparison;
import com.lifescope.dto.HousingPriceResponse;
import com.lifescope.dto.WageResponse;

import lombok.RequiredArgsConstructor;

// 지역 간 생활비 비교 서비스 (핵심 기능) - CPI 비율로 월급 환산 + 지역 간 임금/주거비 비교
//								- 데이터가 없는 지역은 해당 항목을 null 처리 (부분 비교 가능)
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ComparisonService {

	private final CityService cityService;
	private final CpiRepository cpiRepository;
	private final WageRepository wageRepository;
	private final HousingRepository housingRepository;
	
	// 두 지역의 생활비 종합 비교
	public ComparisonResult compare(String fromCode, String toCode, Long monthlySalary) {
		// 1. 두 지역 검증 (없으면 CityService 가 예외 발생)
		City fromCity = cityService.getCityEntity(fromCode);
		City toCity = cityService.getCityEntity(toCode);
		
		// 2. 최신 CPI 각각 조회
		Optional<ConsumerPriceIndex> fromCpi = cpiRepository.findTopByCityCodeOrderByYearMonthDesc(fromCode);
		Optional<ConsumerPriceIndex> toCpi = cpiRepository.findTopByCityCodeOrderByYearMonthDesc(toCode);
		
		// 3. 물가 비율 + 환살 월급 (양쪽 CPI 가 있고 분모가 0이 아닐 때만)
		BigDecimal cpiRatio = null;
		Long adjustedSalary = null;
		if(fromCpi.isPresent() && toCpi.isPresent()) {
			BigDecimal fromValue = fromCpi.get().getCpiValue();
			BigDecimal toValue = toCpi.get().getCpiValue();
			if(fromValue.compareTo(BigDecimal.ZERO) != 0) {
				cpiRatio = toValue.divide(fromValue, 4, RoundingMode.HALF_UP);
				// CPI 엔티티의 adjustSalary() 재활용 (물가 보정 월급)
				adjustedSalary = fromCpi.get().adjustSalary(toValue, monthlySalary)
						.setScale(0, RoundingMode.HALF_UP)
						.longValue();
			}
		}
		
		return ComparisonResult.builder()
				.fromCity(CityResponse.from(fromCity))
				.toCity(CityResponse.from(toCity))
				.cpiRatio(cpiRatio)
				.inputMonthlySalary(monthlySalary)
				.adjustedMonthlySalary(adjustedSalary)
				.wage(buildWageComparison(fromCode, toCode))
				.housing(buildHousingComparison(fromCode, toCode))
				.build();
	}
	
	// 임금 비교 조립 (한 쪽 데이터라도 없는 경우 null)
	private WageComparison buildWageComparison(String fromCode, String toCode) {
		Optional<AverageWage> fromWage = wageRepository.findTopByCityCodeOrderByYearDesc(fromCode);
		Optional<AverageWage> toWage = wageRepository.findTopByCityCodeOrderByYearDesc(toCode);
		if(fromWage.isEmpty() || toWage.isEmpty()) {
			return null;
		}
		return WageComparison.builder()
				.fromWage(WageResponse.from(fromWage.get()))
				.toWage(WageResponse.from(toWage.get()))
				.ratio(divideSafely(toWage.get().getWageAvg(), fromWage.get().getWageAvg()))
				.build();
	}
	
	// 주거비 (전세 기준) 비교 조립 (한 쪽 데이터라도 없는 경우 null)
	private HousingComparison buildHousingComparison(String fromCode, String toCode) {
		Optional<HousingPrice> fromPrice = housingRepository
				.findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(fromCode, "J");
		Optional<HousingPrice> toPrice = housingRepository
				.findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(toCode, "J");
		if(fromPrice.isEmpty() || toPrice.isEmpty()) {
			return null;
		}
		
		return HousingComparison.builder()
				.fromPrice(HousingPriceResponse.from(fromPrice.get()))
				.toPrice(HousingPriceResponse.from(toPrice.get()))
				.ratio(divideSafely(toPrice.get().getAvgPrice(), fromPrice.get().getAvgPrice()))
				.build();
	}
	
	// 0으로 나누기 방지 시스템 (분모가 0인 경우 null)
	private BigDecimal divideSafely(Long numerator, Long denominator) {
		if(denominator == null || denominator == 0) {
			return null;
		}
		return BigDecimal.valueOf(numerator)
				.divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
	}
}
