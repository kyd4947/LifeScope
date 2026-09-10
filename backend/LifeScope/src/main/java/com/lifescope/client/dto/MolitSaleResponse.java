package com.lifescope.client.dto;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 국토부 매매 응답 래퍼 (response > header / body > items > item[])
//		- totalCount = 1 이면 item 이 객체 1개로 오므로 ACCEPT_SINGLE_VALUE_AS_ARRAY 대응
//		- tatalCount = 0 이면 items 가 "" 빈 문자열로 옴 -> 클라이언트에서 사전 감지 후 파싱 스킵
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class MolitSaleResponse {

	@JsonProperty("response")
	private ResponseData response;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class ResponseData{
		@JsonProperty("header")
		private Header header;
		
		@JsonProperty("body")
		private Body body;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Header{
		// "000" 이면 성공
		@JsonProperty("resultCode")
		private String resultCode;
		
		@JsonProperty("resultMsg")
		private String resultMsg;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Body{
		
		@JsonProperty("items")
		private Items items;
		
		@JsonProperty("totalCount")
		private int totalCount;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Items{
		
		// 1 건이면 객체, N건이면 배열 -> 배열로 통일
		@JsonProperty("item")
		@JsonFormat(with=JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		private List<TradeApiItem> item;
	}
}
