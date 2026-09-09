package com.lifescope.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.lifescope.client.dto.CpiApiItem;

// KOSIS OpenAPI 호출 클라이언트 - Spring RestClient 사용, apiKey는 환경변수 KOSIS_API_KEY 에서 주입
@Component
public class KosisClient {

	private final RestClient restClient;
	private final String apiKey;
	private final String orgId;
	private final String tblId;
	private final String itmId;
	
	// 생성자 주입
	public KosisClient(
			RestClient.Builder builder,
			@Value("${kosis.base-url:https://kosis.kr/openapi/Param/statisticsParameterData.do}") String baseUrl,
			@Value("${kosis.api-key:}") String apiKey,
			@Value("${kosis.cpi-org-id:101}") String orgId,
			@Value("${kosis.cpi-tbl-id:INH_1J22003}") String tblId,
			@Value("${kosis.cpi-itm-id:T}") String itmId) {
		this.restClient = builder.baseUrl(baseUrl).build();
		this.apiKey = apiKey;
		this.orgId = orgId;
		this.tblId = tblId;
		this.itmId = itmId;
	}
	
	// 최신(월) CPI 전 지역 조회 (전국 포함 18건 예상)
	public List<CpiApiItem> fetchLatestCpi(){
		if(apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("KOSIS_API_KEY 환경변수가 설정되지 않았습니다.");
		}
		
		List<CpiApiItem> result = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("method", "getList")
						.queryParam("apiKey", apiKey)
						.queryParam("orgId", orgId)
						.queryParam("tblId", tblId)
						.queryParam("objL1", "ALL")
						.queryParam("itmId", itmId)
						.queryParam("format", "json")
						.queryParam("jsonVD", "Y")
						.queryParam("prdSe", "M")
						.queryParam("newEstPrdCnt", 1)
						.queryParam("outputFields", "ORG_ID TBL_ID NM NM_ENG ITM_NM UNIT_NM PRD_DE ")
						.build())
				.retrieve()
				.body(new ParameterizedTypeReference<List<CpiApiItem>>() {});
		return result != null ? result : List.of();
	}
	
}
