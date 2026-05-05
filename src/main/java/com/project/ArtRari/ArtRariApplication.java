package com.project.ArtRari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArtRariApplication {
	public static void main(String[] args) {
		SpringApplication.run(ArtRariApplication.class, args);
	}
}
