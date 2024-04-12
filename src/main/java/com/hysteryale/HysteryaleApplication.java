package com.hysteryale;

import com.hysteryale.rollbar.RollbarInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HysteryaleApplication {

	public static void main(String[] args) {



		SpringApplication.run(HysteryaleApplication.class, args);

	}

}
