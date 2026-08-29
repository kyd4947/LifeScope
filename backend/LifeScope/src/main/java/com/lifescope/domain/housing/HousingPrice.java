package com.lifescope.domain.housing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.lifescope.domain.city.City;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 주거비 실거래가 엔티티
@Entity
@Table(name="housing_price", uniqueConstraints=@UniqueConstraint(columnNames= {"city_code", "trade_type", "year_month"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HousingPrice {
	
	// 기본 키
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	// 연관된 지역
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="city_code", nullable=false)
	private City city;
	
	// 거래 유형 : M=매매, J=전세, W=월세
	@Column(length=1, nullable=false)
	private String tradeType;
	
	// 연월
	@Column(length=6, nullable=false)
	private String yearMonth;
	
	// 평균가 (단위 : 만원)
	@Column(nullable=false)
	private Long avgPrice;
	
	// 거래 건수
	@Column(nullable=false)
	private Integer dealCount;
	
	// 생성 시각
	@CreationTimestamp
	@Column(nullable=false)
	private LocalDateTime createdAt;
	
	/**
	 * 거래 유형 한글 표기
	 */
	public String getTradeTypeLabel() {
		switch(tradeType) {
			case "M" : return "매매";
			case "J" : return "전세";
			case "W" : return "월세";
			default : return tradeType;
		}
	}
	
	/**
	 * 해당 지역 최근 N 건 평균가
	 */
	public static Long averageOf(List<HousingPrice> prices) {
		if(prices==null || prices.isEmpty()) return null;
		return Math.round(prices.stream()
				.mapToLong(HousingPrice::getAvgPrice)
				.average()
				.orElse(0.0));
	}
	
	@Override
	public boolean equals(Object o) {
		if(this==o) return true;
		if(!(o instanceof HousingPrice)) return false;
		HousingPrice that = (HousingPrice) o;
		return Objects.equals(city, that.city)
				&& Objects.equals(tradeType, that.tradeType)
				&& Objects.equals(yearMonth, that.yearMonth);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(city, tradeType, yearMonth);
	}
}
