package com.lifescope.client;

import java.util.Map;
import java.util.Optional;

// KOSIS 지역명(C1_NM) -> city.code 변환표 - 응답에 지역코드가 없으므로 한글명 기준 매핑 (17개 시/도)
public class KosisRegionMapper {

	// 집계 행 (DB 저장 제외)
	private static final String NATIONWIDE = "전국";
	
	// KOSIS 지역명 -> 법정동 5 자리 코드 (Map.of 는 10개 제한이라 ofEntries 사용)
	private static final Map<String, String> NAME_TO_CODE = Map.ofEntries(
			Map.entry("서울특별시", "11000"),
			Map.entry("부산광역시", "26000"),
			Map.entry("대구광역시", "27000"),
			Map.entry("인천광역시", "28000"),
			Map.entry("광주광역시", "29000"),
			Map.entry("대전광역시", "30000"),
			Map.entry("울산광역시", "31000"),
			Map.entry("세종특별자치시", "36000"),
			Map.entry("경기도", "41000"),
			Map.entry("강원특별자치도", "42000"),
			Map.entry("충청북도", "43000"),
			Map.entry("충청남도", "44000"),
			Map.entry("전북특별자치도", "45000"),
			Map.entry("전라남도", "46000"),
			Map.entry("경상북도", "47000"),
			Map.entry("경상남도", "48000"),
			Map.entry("제주특별자치도", "50000"));
	
	// 인스턴스화 방지 (유틸 클래스)
	private KosisRegionMapper() {}
	
	// 지역명을 city.code 로 변환 (전국·미등록명은 empty)
	public static Optional<String> toCityCode(String regionName){
		if(regionName == null || NATIONWIDE.equals(regionName.trim())) {
			return Optional.empty();
		}
		return Optional.ofNullable(NAME_TO_CODE.get(regionName.trim()));
	}
}
