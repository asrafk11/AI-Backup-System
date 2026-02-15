package com.aibackup.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiBackupSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBackupSystemApplication.class, args);
	}

}
