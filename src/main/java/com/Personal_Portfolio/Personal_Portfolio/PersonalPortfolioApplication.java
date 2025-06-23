package com.Personal_Portfolio.Personal_Portfolio;

import com.Personal_Portfolio.Personal_Portfolio.Config.GoogleOAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(GoogleOAuthConfig.class)
@EnableScheduling // This enables @Scheduled annotation for market data fetching
public class PersonalPortfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalPortfolioApplication.class, args);
	}

}
