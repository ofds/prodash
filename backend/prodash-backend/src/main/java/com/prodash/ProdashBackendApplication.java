package com.prodash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the ProDash Spring Boot application.
 * @EnableScheduling is crucial for enabling the periodic data synchronization tasks.
 */
@SpringBootApplication
@EnableScheduling
public class ProdashBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProdashBackendApplication.class, args);
	}

}
