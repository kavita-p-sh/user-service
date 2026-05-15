package com.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.ecommerce.user",
		"com.ecommerce.common"
})

@EntityScan(basePackages = {
		"com.ecommerce.common.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.ecommerce.user.repository",
		"com.ecommerce.common.repository"
})


public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
