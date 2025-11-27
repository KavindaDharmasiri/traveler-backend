package com.traveler.auth.traveler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"com.traveler.auth.traveler.entity", "com.traveler.common.entity"})
public class TravelerAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerAuthApplication.class, args);
	}

}
