package com.lifescope.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.lifescope.client.dto.MolitRentResponse;
import com.lifescope.client.dto.MolitSaleResponse;
import com.lifescope.client.dto.RentApiItem;
import com.lifescope.client.dto.TradeApiItem;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import lombok.extern.slf4j.Slf4j;

// 국토부(공공데이터포털) 실거래가 API 클라이언트
// 		- 매매 : getRTMDataSvcAptTradeDev (아파트 매매 실거래)
//		- 전월세 : getRTMDataSvcAptRent (아파트 전월세 실거래)
//		- 주의 : 공공데이터포털 키는 URL 인코딩 상태라 RestClient 인코딩 시 이중 인코딩 됨 -> 문자열 url 조립 후 URI.create() 로 인코딩 우회
@Slf4j
@Component
public class MolitClient {

	private static final String SALE_OPERATION = "getRTMDataSvcAptTradeDev";
	private static final String RENT_OPERATION = "getRTMDataSvcAptRent";
	
	private final RestClient restClient;
	private final String baseUrl;
	private final String serviceKey;
	private final int numOfRows;
	private final Retry publicApiRetry;
	private final CircuitBreaker publicApiCircuitBreaker;
	
	public MolitClient(
			RestClient.Builder builder,
			@Value("${molit.base-url}") String baseUrl,
			@Value("${molit.api-key}") String serviceKey,
			@Value("${molit.num-of-rows:1000}") int numOfRows,
			Retry publicApiRetry,
			CircuitBreaker publicApiCircuitBreaker) {
		this.restClient = builder
				.baseUrl(baseUrl)
				.defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) LifeScope/1.0")
				.defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
				.build();
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.serviceKey = serviceKey;
		this.numOfRows = numOfRows;
		this.publicApiRetry = publicApiRetry;
		this.publicApiCircuitBreaker = publicApiCircuitBreaker;
	}
	
	// 아파트 매매 실거래가 전체 조회 (페이지 순회)
	public List<TradeApiItem> fetchAptSale(String lawdCode, String dealYmd){
		checkApiKey();
		List<TradeApiItem> allItems = new ArrayList<>();
		int pageNo = 1;
		while(true) {
			MolitSaleResponse page = getPage(SALE_OPERATION, lawdCode, dealYmd, pageNo, MolitSaleResponse.class);
			if(!"000".equals(page.getResponse().getHeader().getResultCode())) {
				throw new IllegalStateException("MOLIT 매매 API 오류 : " + page.getResponse().getHeader().getResultCode() + " " + page.getResponse().getHeader().getResultMsg());
			}
			MolitSaleResponse.Body body = page.getResponse().getBody();
			// totalCount = 0 이면 items 가 " " 빈 문자열로 옴 -> 수집 종료
			if(body.getTotalCount() == 0 || body.getItems() == null || body.getItems().getItem() == null) {
				break;
			}
			allItems.addAll(body.getItems().getItem());
			if(allItems.size() >= body.getTotalCount()) {
				break;
			}
			pageNo++;
		}
		return allItems;
	}
	
	// 아파트 전월세 실거래가 전체 조회 (페이지 순회)
	public List<RentApiItem> fetchAptRent(String lawdCode, String dealYmd){
		checkApiKey();
		List<RentApiItem> allItems = new ArrayList<>();
		int pageNo = 1;
		while(true) {
			MolitRentResponse page = getPage(RENT_OPERATION, lawdCode, dealYmd, pageNo, MolitRentResponse.class);
			if(!"000".equals(page.getResponse().getHeader().getResultCode())) {
				throw new IllegalStateException("MOLIT 전월세 API 오류 : " + page.getResponse().getHeader().getResultCode() + " " + page.getResponse().getHeader().getResultMsg());
			}
			MolitRentResponse.Body body = page.getResponse().getBody();
			// totalCount = 0 이면 items 가 "" 빈 문자열로 옴 -> 수집 종료
			if(body.getTotalCount() == 0 || body.getItems() == null || body.getItems().getItem() == null) {
				break;
			}
			allItems.addAll(body.getItems().getItem());
			if(allItems.size() >= body.getTotalCount()) {
				break;
			}
			pageNo++;
		}
		return allItems;
	}
	
	// 단일 페이지 조회 - 문자열 URL 직접 조립 후 URI.create() 로 인코딩 우회
	// 서킷 최외곽 : OPEN 상태면 재시도 없이 즉시 차단, CLOSED 면 재시도로 실행
	private <R> R getPage(String operation, String lawdCode, String dealYmd, int pageNo, Class<R> responseType) {
		return publicApiCircuitBreaker.decorateSupplier(
				publicApiRetry.decorateSupplier(() -> {
					String url = baseUrl + "/" + operation
							+ "?serviceKey=" + serviceKey
							+ "&LAWD_CD=" + lawdCode
							+ "&DEAL_YMD=" + dealYmd
							+ "&pageNo=" + pageNo
							+ "&numOfRows=" + numOfRows
							+ "&_type=json";
					return restClient.get().uri(URI.create(url)).retrieve().body(responseType);
				})).get();
	}
	
	// API 키 사전 검증
	private void checkApiKey() {
		if(serviceKey == null || serviceKey.isBlank()) {
			throw new IllegalStateException("MOLIT_API_KEY 환경변수가 설정되지 않았습니다.");
		}
	}
}