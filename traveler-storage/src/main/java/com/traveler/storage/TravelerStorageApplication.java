package com.traveler.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"com.traveler.storage.entity", "com.traveler.common.entity"})
@EnableJpaRepositories(basePackages = {"com.traveler.storage.repository", "com.traveler.common.repository"})
public class TravelerStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerStorageApplication.class, args);
	}

}
