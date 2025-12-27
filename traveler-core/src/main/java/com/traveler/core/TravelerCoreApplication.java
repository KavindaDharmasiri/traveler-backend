package com.traveler.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EntityScan("com.traveler.common.entity")
public class TravelerCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerCoreApplication.class, args);
	}

}
