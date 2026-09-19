package com.lifescope.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifescope.dto.SyncResult;
import com.lifescope.service.DataSyncService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

// 관리자용 데이터 동기화 API - 수동 트리거
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final DataSyncService dataSyncService;
	
	// 전체 공공 데이터 수동 동기화
	@Operation(summary="데이터 수동 동기화", description="KOSIS CPI/임금 + 국토부 주거비 전체 수집 트리거")
	@PostMapping("/sync")
	public ResponseEntity<SyncResult> sync(){
		return ResponseEntity.ok(dataSyncService.syncAll());
	}
}
