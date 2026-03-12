package com.trading.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class StockTradingPlatformApplication {

	public static void main(String[] args) {

        SpringApplication.run(StockTradingPlatformApplication.class, args);


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin@123"));
	}

}
