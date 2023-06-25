package com.junioroffers;

import com.junioroffers.infrastructure.security.jwt.JwtConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableConfigurationProperties({JwtConfigurationProperties.class})
@EnableMongoRepositories
public class MyJobOffersSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyJobOffersSpringBootApplication.class, args);
    }
}
