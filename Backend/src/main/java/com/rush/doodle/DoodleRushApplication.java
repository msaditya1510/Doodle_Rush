package com.rush.doodle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DoodleRushApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoodleRushApplication.class, args);
	}

}
