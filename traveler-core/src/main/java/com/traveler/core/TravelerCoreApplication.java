package com.traveler.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EntityScan("com.traveler.common.entity")
public class TravelerCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerCoreApplication.class, args);
	}

}
