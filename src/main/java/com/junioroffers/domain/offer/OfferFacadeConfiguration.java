package com.junioroffers.domain.offer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class OfferFacadeConfiguration {

    @Bean
    OfferFacade offerFacade(OfferFetchable offerFetchable, OfferRepository repo) {
        OfferService offerService = new OfferService(offerFetchable, repo);
        return new OfferFacade(repo, offerService);
    }
}
