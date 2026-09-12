package com.lifescope.scheduler;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.lifescope.service.CpiCollectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 기동 시 1회 CPI 수집 러너 - kosis.collect-on-startup=true 일 때만 동작
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="kosis.collect-on-startup", havingValue="true")
public class CpiCollectionRunner implements ApplicationRunner {

	private final CpiCollectionService collectionService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("KOSIS CPI 수집 시작");
		int count = collectionService.collectLatestCpi();
		log.info("KOSIS CPI 수집 완료 : {} 건 저장/갱신", count);
	}
}
