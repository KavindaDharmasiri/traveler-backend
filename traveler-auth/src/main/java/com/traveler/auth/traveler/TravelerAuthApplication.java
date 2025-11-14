package com.traveler.auth.traveler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TravelerAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerAuthApplication.class, args);
	}

}
