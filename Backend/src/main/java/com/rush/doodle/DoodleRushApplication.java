package com.rush.doodle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableScheduling
@SpringBootApplication
public class DoodleRushApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoodleRushApplication.class, args);
	}
	 @Bean
	     WebMvcConfigurer corsConfigurer() {
	        return new WebMvcConfigurer() {
	            @Override
	            public void addCorsMappings(CorsRegistry registry) {
	                registry.addMapping("/api/**")
	                        .allowedOrigins("https://doodle-rush.vercel.app/","https://doodle-rush.vercel.app","http://localhost:5173")
	                        .allowedMethods("*");
	            }
	        };

	 }
}