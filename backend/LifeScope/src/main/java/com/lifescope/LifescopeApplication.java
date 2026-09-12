package com.lifescope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling					// 월간 데이터 동기화 @Scheduled 활성화
public class LifescopeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LifescopeApplication.class, args);
	}

}
