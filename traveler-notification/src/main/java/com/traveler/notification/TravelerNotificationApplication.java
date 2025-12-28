package com.traveler.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.traveler.common.entity", "com.traveler.notification.entity"})
@EnableJpaRepositories(basePackages = {"com.traveler.notification.repository", "com.traveler.common.repository"})
@EnableFeignClients
public class TravelerNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerNotificationApplication.class, args);
	}

}
