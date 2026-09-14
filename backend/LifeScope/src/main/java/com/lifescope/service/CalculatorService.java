package com.lifescope.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lifescope.domain.costOfLiving.ConsumerPriceIndex;
import com.lifescope.domain.costOfLiving.CpiRepository;
import com.lifescope.domain.housing.HousingPrice;
import com.lifescope.domain.housing.HousingRepository;
import com.lifescope.dto.CostAdjustRequest;
import com.lifescope.dto.CostAdjustResponse;
import com.lifescope.dto.HousingAdjustedRequest;
import com.lifescope.dto.HousingAdjustedResponse;
import com.lifescope.dto.RealIncomeResponse;
import com.lifescope.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;

// 생활비 계산기 서비스 - 실수령액 / 물가 보정 / 주거비 포함 실질 구매력 계산
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalculatorService {

	// 국민연금 근로자 부담율 (소득 월액 기준)
	private static final BigDecimal PENSION_RATE = new BigDecimal("0.045");
	
	// 국민연금 월소득 상한 (2025년 기준)
	private static final long PENSION_CAP = 6_170_000L;
	
	// 건강보험 근로자 부담율 (총 7.09% 중 절반)
	private static final BigDecimal HEALTH_RATE = new BigDecimal("0.03545");
	
	// 장기요양보험 - 건강보험료의 12.95%
	private static final BigDecimal CARE_RATE = new BigDecimal("0.1295");
	
	// 고용보험 근로자 부담율
	private static final BigDecimal EMPLOYMENT_RATE = new BigDecimal("0.009");
	
	// 전월세 전환율 (전세 보증금 -> 월세 환산, 업계 표준 근사치)
	private static final BigDecimal JEONSE_CONVERT_RATE = new BigDecimal("0.04");
	
	// 소득세 근사 계산용 공제액 (근로소득공제 + 기본 공제 월 환산 근사치)
	private static final long MONTHLY_DEDUCTION = 1_500_000L;
	
	private final CityService cityService;
	private final CpiRepository cpiRepository;
	private final HousingRepository housingRepository;
	
	// 연봉 -> 월 실수령액 계산 (4대 보험 + 소득세 공제)
	public RealIncomeResponse realIncome(Long annualSalary) {
		long monthlyGross = BigDecimal.valueOf(annualSalary)
				.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP)
				.longValue();
		
		// 국민연금 (상한 적용)
		long pension = scale(BigDecimal.valueOf(Math.min(monthlyGross, PENSION_CAP))
				.multiply(PENSION_RATE));
		
		// 건강보험
		long health = scale(BigDecimal.valueOf(monthlyGross).multiply(HEALTH_RATE));
		
		// 장기요양 (건강보험료 기준)
		long care = scale(BigDecimal.valueOf(health).multiply(CARE_RATE));
		
		// 고용보험
		long employment = scale(BigDecimal.valueOf(monthlyGross).multiply(EMPLOYMENT_RATE));
		
		// 소득세
		long incomeTax = incomeTax(monthlyGross);
		
		// 월 실수령액
		long totalDeduction = pension + health + care + employment + incomeTax;
		long monthlyNet = monthlyGross - totalDeduction;
		
		return RealIncomeResponse.builder()
				.annualSalary(annualSalary)
				.monthlyGross(monthlyGross)
				.pension(pension)
				.health(health)
				.care(care)
				.employment(employment)
				.incomeTax(incomeTax)
				.monthlyNet(monthlyNet)
				.totalDeduction(totalDeduction)
				.build();
	}
	
	// 물가 보정 환산 - 기준 지역 월급으로 대상 지역 동등 생활 수준 금액 계산
	public CostAdjustResponse costAdjust(CostAdjustRequest request) {
		String fromCode = request.getFromCode();
		String toCode = request.getToCode();
		
		// 1. 지역 검증 (없으면 CityService 가 404 예외 발생)
		var fromCity = cityService.getCityEntity(fromCode);
		var toCity = cityService.getCityEntity(toCode);
		
		// 2. 양쪽 최신 CPI 필수 (없으면 계산 불가 -> 404)
		ConsumerPriceIndex fromCpi = cpiRepository
				.findTopByCityCodeOrderByYearMonthDesc(fromCode)
				.orElseThrow(() -> new DataNotFoundException("물가 지수 데이터가 없습니다. 지역 코드 : " + fromCode));
		ConsumerPriceIndex toCpi = cpiRepository
				.findTopByCityCodeOrderByYearMonthDesc(toCode)
				.orElseThrow(() -> new DataNotFoundException("물가 지수 데이터가 없습니다. 지역 코드 : " + toCode));
		
		// 3. 비율 + 환산 (ComparisonService 와 동일 로직 재사용)
		BigDecimal ratio = toCpi.getCpiValue().divide(fromCpi.getCpiValue(), 4, RoundingMode.HALF_UP);
		Long adjusted = fromCpi.adjustSalary(toCpi.getCpiValue(), request.getMonthlySalary())
				.setScale(0, RoundingMode.HALF_UP)
				.longValue();
		
		return CostAdjustResponse.builder()
				.fromCity(com.lifescope.dto.CityResponse.from(fromCity))
				.toCity(com.lifescope.dto.CityResponse.from(toCity))
				.cpiRatio(ratio)
				.inputSalary(request.getMonthlySalary())
				.adjustedSalary(adjusted)
				.build();
	}
	
	// 주거비 포함 비교 - 주거비 차액 반영 실질 구매력 계산
	public HousingAdjustedResponse housingAdjusted(HousingAdjustedRequest request) {
		String fromCode = request.getFromCode();
		String toCode = request.getToCode();
		String tradeType = request.getTradeType();
		
		// 1. 지역 검증
		var fromCity = cityService.getCityEntity(fromCode);
		var toCity = cityService.getCityEntity(toCode);
		
		// 2. 양쪽 최신 주거비 조회 (데이터 없으면 404 - 부분 계산 불가)
		HousingPrice fromPrice = housingRepository
				.findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(fromCode, tradeType)
				.orElseThrow(() -> new DataNotFoundException("주거비 데이터가 없습니다. 지역 코드 : " + fromCode + ", 거래 유형 : " + tradeType));
		
		HousingPrice toPrice = housingRepository
				.findTopByCityCodeAndTradeTypeOrderByYearMonthDesc(toCode, tradeType)
				.orElseThrow(() -> new DataNotFoundException("주거비 데이터가 없습니다. 지역 코드 : " + toCode + ", 거래 유형 : " + tradeType));
		
		// 3. 월 주거비 환산 (avgPrice 단위 = 만원)
		long fromMonthlyCost = monthlyHousingCost(fromPrice, tradeType);
		long toMonthlyCost = monthlyHousingCost(toPrice, tradeType);
		
		// 4. 실질 구매력 = 월급 - (대상 지역 주거비 - 기준 지역 주거비)
		long realPurchasingPower = request.getMonthlySalary() - (toMonthlyCost - fromMonthlyCost);
		
		return HousingAdjustedResponse.builder()
				.fromCity(com.lifescope.dto.CityResponse.from(fromCity))
				.toCity(com.lifescope.dto.CityResponse.from(toCity))
				.tradeType(tradeType)
				.tradeTypeLabel(toPrice.getTradeTypeLabel())
				.inputSalary(request.getMonthlySalary())
				.fromHousingPrice(fromPrice.getAvgPrice())
				.toHousingPrice(toPrice.getAvgPrice())
				.fromMonthlyHousingCost(fromMonthlyCost)
				.toMonthlyHousingCost(toMonthlyCost)
				.realPurchasingPower(realPurchasingPower)
				.build();
	}
	
	// 월 주거비 환산 - 월세는 그대로, 전세 / 매매는 전월세 전환율 4%로 월 환산
	private long monthlyHousingCost(HousingPrice price, String tradeType) {
		long priceWon = price.getAvgPrice() * 10_000L;			// 만원 -> 원
		if("W".equals(tradeType)) {
			return priceWon;				// 월세 평균은 월 금액 그대로
		}
		return scale(BigDecimal.valueOf(priceWon)
				.multiply(JEONSE_CONVERT_RATE)
				.divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP));
	}
	
	// 간이 누진 소득세 (월 기준, 근로 소득 공제 + 기본 공제 월 150만원 가정)
	private long incomeTax(long monthlyGross) {
		long taxable = Math.max(0, monthlyGross - MONTHLY_DEDUCTION);
		if(taxable <= 0) return 0;
		if(taxable <= 1_166_666L) {			// ~1,400 만원/년 구간 6%
			 return scale(BigDecimal.valueOf(taxable).multiply(new BigDecimal("0.06")));
		}
		if(taxable <= 3_833_333L) {			// 1,400 ~ 4,600 만원 15%
			return scale(BigDecimal.valueOf(taxable).multiply(new BigDecimal("0.15")));
		}
		if(taxable <= 7_333_333L) {			// 4,600 ~ 8,800 만원 24%
			return scale(BigDecimal.valueOf(taxable).multiply(new BigDecimal("0.24")));
		}
		if(taxable <= 12_500_000L) {		// 8,800 만 ~ 1.5 억 35%
			return scale(BigDecimal.valueOf(taxable).multiply(new BigDecimal("0.35")));
		}
		// 1.5 억 초과 38%
		return scale(new BigDecimal("3118333")
				.add(BigDecimal.valueOf(taxable).multiply(new BigDecimal("0.38"))));
	}
	
	// BigDecimal -> long (반올림)
	private long scale(BigDecimal value) {
		return value.setScale(0, RoundingMode.HALF_UP).longValue();
	}
}
