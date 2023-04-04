package com.junioroffers;

//import com.junioroffers.infrastructure.offer.http.OfferClientConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
//@EnableConfigurationProperties({OfferClientConfigProperties.class})
@EnableMongoRepositories
public class MyJobOffersSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyJobOffersSpringBootApplication.class, args);
    }
}
