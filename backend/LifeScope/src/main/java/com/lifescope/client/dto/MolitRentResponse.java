package com.lifescope.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 국토부 전월세 응답 래퍼 (response > header / body > items > item[])
//		- 매매 응답(MolitSaleResponse)과 구조 동일, item 타입만 전월세용
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MolitRentResponse {

	@JsonProperty("response")
	private ResponseData response;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ResponseData {
		@JsonProperty("header")
		private Header header;
		
		@JsonProperty("body")
		private Body body;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
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
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body{
		@JsonProperty("items")
		private Items items;
		
		@JsonProperty("totalCount")
		private int totalCount;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items{
		
		// 1 건이면 객체, N 건이면 배열 -> 배열로 통일
		@JsonProperty("item")
		@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		private List<RentApiItem> item;
	}
}
