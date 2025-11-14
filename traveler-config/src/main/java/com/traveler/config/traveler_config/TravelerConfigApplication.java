package com.traveler.config.traveler_config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class TravelerConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelerConfigApplication.class, args);
	}

}
