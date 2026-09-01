package com.lifescope.domain.city;

import java.util.ArrayList;
import java.util.List;

import com.lifescope.domain.costOfLiving.ConsumerPriceIndex;
import com.lifescope.domain.housing.HousingPrice;
import com.lifescope.domain.wage.AverageWage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="city")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

	@Id
	@Column(length=10)
	private String code;
	
	@Column(nullable=false, length=50)
	private String name;
	
	@Column(length=10)
	private String parentCode;
	
	@Column(nullable=false)
	private Short level;
	
	private Double latitude;
	private Double longitude;
	
	@Builder.Default
	@Column(nullable=false)
	private Boolean isActive = true;
	
	@OneToMany(mappedBy="city", cascade=CascadeType.ALL, orphanRemoval=true)
	@Builder.Default
	private List<ConsumerPriceIndex> cpiList = new ArrayList<>();

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AverageWage> wageList = new ArrayList<>();

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HousingPrice> housingList = new ArrayList<>();
}
