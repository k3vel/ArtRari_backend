package com.project.ArtRari;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ArtRariApplication {
	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(ArtRariApplication.class, args);
//		UserRepository userRepository = (UserRepository) ac.getBean(UserRepository.class);
//		User someUser = userRepository.findByEmail("abc").get();
//		System.out.println(someUser);
	}
}
