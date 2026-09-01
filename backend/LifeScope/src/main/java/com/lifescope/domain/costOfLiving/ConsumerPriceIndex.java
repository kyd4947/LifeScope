package com.lifescope.domain.costOfLiving;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

@Entity
@Table(name = "consumer_price_index", 
uniqueConstraints = @UniqueConstraint(columnNames = {"city_code", "base_year", "year_month"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerPriceIndex {

	// 기본키 (자동 생성)
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	// 연관 지역
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="city_code", nullable=false)
	private City city;
	
	// 기준 년도
	@Column(nullable=false)
	private Short baseYear;
	
	// 연월 (예: 202412) - DB 스키마의 CHAR(6) 타입과 일치
	@Column(length=6, columnDefinition="CHAR(6)", nullable=false)
	private String yearMonth;
	
	// 물가지수 값
	@Column(nullable=false, precision=10, scale=4)
	private BigDecimal cpiValue;
	
	// 생성 시각
	@CreationTimestamp
	@Column(nullable=false, updatable=false)
	private LocalDateTime createdAt;
	
	/**
	 * 물가 보정 환산 : 해당 CPI 비율로 목표 지역의 월급 환산
	 */
	
	public BigDecimal adjustSalary(BigDecimal targetCPI, Long salary) {
		if(this.cpiValue.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.valueOf(salary);
		}
		return BigDecimal.valueOf(salary)
				.multiply(targetCPI)
				.divide(this.cpiValue, 2, RoundingMode.HALF_UP);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof ConsumerPriceIndex)) return false;
		ConsumerPriceIndex that = (ConsumerPriceIndex) o;
		return Objects.equals(city,  that.city)
				&& Objects.equals(baseYear, that.baseYear)
				&& Objects.equals(yearMonth, that.yearMonth);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(city, baseYear, yearMonth);
	}
}
