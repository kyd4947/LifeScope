package com.lifescope.domain.wage;

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

// 지역별 평균 임금 엔티티
@Entity
@Table(name="average_wage", uniqueConstraints=@UniqueConstraint(columnNames= {"city_code", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AverageWage {
	
	// 기본키
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	// 연관된 지역
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="city_code", nullable=false)
	private City city;
	
	// 기준 연도
	@Column(nullable=false)
	private Short year;
	
	// 연평균 임금
	@Column(nullable=false)
	private Long wageAvg;
	
	// 월평균 임금 - DB에서 GENERATED ALWAYS로 자동 계산되는 컬럼 (INSERT/UPDATE 시 값 지정 불가)
	@Column(nullable=false, insertable=false, updatable=false)
	private Long wageMonthly;
	
	// 생성 시각
	@CreationTimestamp
	@Column(nullable=false, updatable=false)
	private LocalDateTime createdAt;
	
	/**
	 * 엔티티 생성 시 월 평균 임금 자동 계산
	 */
	public AverageWage(City city, Short year, Long wageAvg) {
		this.city = city;
		this.year = year;
		this.wageAvg = wageAvg;
		this.wageMonthly = wageAvg / 12;
	}
	
	/**
	 * 월급 대비 시/도 별 상대값 환산
	 */
	public Double compareTo(Long targetAvg) {
		if(targetAvg == null || targetAvg == 0) return null;
		return Math.round((this.wageAvg.doubleValue() / targetAvg) * 100.0) / 100.0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof AverageWage)) return false;
		AverageWage that = (AverageWage) o;
		return Objects.equals(city, that.city)
				&& Objects.equals(year, that.year);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(city, year);
	}
}
