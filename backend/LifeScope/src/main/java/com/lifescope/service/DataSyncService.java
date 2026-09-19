package com.lifescope.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lifescope.dto.SyncResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 데이터 동기화 오케스트레이터 - CPI/주거비/임금 수집 통합 실행
//						- 항목별 실패는 격리해 나머지 수빚 계속 (부분 성공 허용)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

	private final CpiCollectionService cpiCollectionService;
	private final HousingCollectionService housingCollectionService;
	private final WageCollectionService wageCollectionService;

	// 전체 동기화 실행 (수동 트리거 + 월간 스케줄 공용)
	public SyncResult syncAll() {
		SyncResult.SyncResultBuilder builder = SyncResult.builder();
		List<String> errors = new ArrayList<>();
		int cpiCount = 0;
		int housingCount = 0;
		int wageCount = 0;

		// 1. CPI (KOSIS)
		try {
			cpiCount = cpiCollectionService.collectLatestCpi();
		} catch (Exception e) {
			log.error("CPI 동기화 실패", e);
			errors.add("CPI : " + e.getMessage());
		}

		// 2. 주거비 (국토부) - 통계 확정 기준 지난달 (예 : 9월 1일 실행 시 202508)
		try {
			String dealYmd = YearMonth.now().minusMonths(1)
					.format(DateTimeFormatter.ofPattern("yyyyMM"));
			housingCount = housingCollectionService.collectMonthlyHousing(dealYmd);
		} catch (Exception e) {
			log.error("주거비 동기화 실패", e);
			errors.add("주거비 : " + e.getMessage());
		}

		// 3. 임금 (KOSIS)
		try {
			wageCount = wageCollectionService.collectLatestWage();
		} catch (Exception e) {
			log.error("임금 동기화 실패", e);
			errors.add("임금 : " + e.getMessage());
		}

		return builder.cpiCount(cpiCount).housingCount(housingCount).wageCount(wageCount).errors(errors).build();
	}
}
