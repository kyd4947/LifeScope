package com.lifescope.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 데이터 동기화 실행 결과 응답 DTO - 항목별 성공 / 실패 공유 (부분 성공 허용)
@Getter
@Builder
@AllArgsConstructor
public class SyncResult {

	// CPI 저장 / 갱신 건수
	@Builder.Default
	private final int cpiCount = 0;
	
	// 주거비 저장 / 갱신 건수
	@Builder.Default
	private final int housingCount = 0;
	
	// 임금 저장 / 갱신 건수
	@Builder.Default
	private final int wageCount = 0;
	
	// 항목별 실패 사유 (빈 리스트면 전체 성공)
	@Builder.Default
	private final List<String> errors = new ArrayList<>();
}
