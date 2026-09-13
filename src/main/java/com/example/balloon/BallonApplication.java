package com.example.balloon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BallonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BallonApplication.class, args);
	}

}
