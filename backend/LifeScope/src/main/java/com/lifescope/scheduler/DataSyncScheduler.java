package com.lifescope.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lifescope.dto.SyncResult;
import com.lifescope.service.DataSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 월간 데이터 자동 동기화 스케줄러 - 매월 1일 07:00 실행
//							- AtomicBoolean 으로 실행 중 중복 진입 방지 (단일 인스턴스 기준)
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncScheduler {

	private final DataSyncService dataSyncService;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	// cron = 초 분 시 일 월 요일
	@Scheduled(cron = "0 0 7 1 * *")
	public void monthlySync() {
		if(!running.compareAndSet(false, true)) {
			log.warn("이전 동기화가 아직 실행 중 - 이번 스케줄 스킵");
			return;
		}
		try {
			log.info("월간 데이터 동기화 시작");
			SyncResult result = dataSyncService.syncAll();
			log.info("월간 데이터 동기화 완료 : CPI {}  건, 주거비 {} 건, 임금 {} 건, 오류 {} 개",
					result.getCpiCount(), result.getHousingCount(), result.getWageCount(),
					result.getErrors().size());
		} catch(Exception e) {
			log.error("월간 데이터 동기화 실패", e);
		} finally {
			running.set(false);
		}
	}
}
