package com.lifescope.dto;

import com.lifescope.domain.city.City;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 지역 조회 응답 DTO : 엔티티의 연관 컬렉션 (cpiList 등)은 제외해 응답 경량화
@Getter
@Builder
@AllArgsConstructor
public class CityResponse {

	// 법정동 코드 앞 5 자리
	private final String code;
	
	// 지역명
	private final String name;
	
	// 상위 지역 코드 (시/도는 null)
	private final String parentCode;
	
	// 계층 1 = 시/도, 2 = 시/군/구
	private final Short level;
	
	// 위도 (지도 시각화용)
	private final Double latitude;
	
	// 경도
	private final Double longitude;
	
	// 사용 여부
	private final Boolean isActive;
	
	// 엔티티 -> DTO 변환
	public static CityResponse from(City city) {
		return CityResponse.builder()
				.code(city.getCode())
				.name(city.getName())
				.parentCode(city.getParentCode())
				.level(city.getLevel())
				.latitude(city.getLatitude())
				.longitude(city.getLongitude())
				.isActive(city.getIsActive())
				.build();
	}
}
