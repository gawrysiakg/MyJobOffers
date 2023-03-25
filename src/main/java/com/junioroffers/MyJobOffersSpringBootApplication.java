package com.junioroffers;

import com.junioroffers.infrastructure.offer.http.OfferClientConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OfferClientConfigProperties.class})
public class MyJobOffersSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyJobOffersSpringBootApplication.class, args);
    }
}
