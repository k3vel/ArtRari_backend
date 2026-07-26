package com.project.ArtRari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ArtRariApplication {
	public static void main(String[] args) {
		SpringApplication.run(ArtRariApplication.class, args);
	}
}

/*todo верификация аккаунта при регистрации,
*  система оплаты (демо версия),
*  КРАСИВАЯ ПРЕЗЕНТАЦИЯ */