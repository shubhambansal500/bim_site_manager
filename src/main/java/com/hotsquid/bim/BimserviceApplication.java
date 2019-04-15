package com.hotsquid.bim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages= {"com.hotsquid"})
public class BimserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BimserviceApplication.class, args);
	}
}
