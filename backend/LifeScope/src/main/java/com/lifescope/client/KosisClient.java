package com.lifescope.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import com.lifescope.client.dto.CpiApiItem;

// KOSIS OpenAPI 호출 클라이언트 - Spring RestClient 사용, apiKey는 환경변수 KOSIS_API_KEY 에서 주입
@Component
public class KosisClient {

	private final RestClient restClient;
	private final String apiKey;
	private final String orgId;
	private final String tblId;
	private final String itmId;
	private final JsonMapper jsonMapper;
	
	// 생성자 주입
	public KosisClient(
			RestClient.Builder builder,
			JsonMapper jsonMapper,
			@Value("${kosis.base-url:https://kosis.kr/openapi/Param/statisticsParameterData.do}") String baseUrl,
			@Value("${kosis.api-key:}") String apiKey,
			@Value("${kosis.cpi-org-id:101}") String orgId,
			@Value("${kosis.cpi-tbl-id:INH_1J22003}") String tblId,
			@Value("${kosis.cpi-itm-id:T}") String itmId) {
		this.restClient = builder
				.baseUrl(baseUrl)
				// KOSIS가 자바 기본 User-Agent 요청에 HTML을 반환하므로 브라우저 식별자로 고정
				.defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) LifeScope/1.0")
				.build();
		this.apiKey = apiKey;
		this.orgId = orgId;
		this.tblId = tblId;
		this.itmId = itmId;
		this.jsonMapper = jsonMapper;
	}
	
	// 최신(월) CPI 전 지역 조회 (전국 포함 18건 예상)
	public List<CpiApiItem> fetchLatestCpi(){
		if(apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("KOSIS_API_KEY 환경변수가 설정되지 않았습니다.");
		}
		
		String rawBody = restClient.get()
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
				.body(String.class);
		try {
			List<CpiApiItem> result = jsonMapper.readValue(rawBody, new TypeReference<List<CpiApiItem>>() {});
			return result != null ? result : List.of();			
		} catch (JacksonException e) {
			String preview = rawBody.length() > 200 ? rawBody.substring(0, 200) : rawBody;
			throw new IllegalStateException("KOSIS 응답 파싱 실패. 응답 앞부분 : " + preview, e);
		}
	}
	
}
