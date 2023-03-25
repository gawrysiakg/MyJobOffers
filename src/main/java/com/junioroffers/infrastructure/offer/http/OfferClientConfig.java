package com.junioroffers.infrastructure.offer.http;

import com.junioroffers.domain.offer.OfferFetchable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
@RequiredArgsConstructor
@Configuration
public class OfferClientConfig {


    private final OfferClientConfigProperties offerClientConfigProperties;



    @Bean
    public RestTemplateResponseErrorHandler restTemplateResponseErrorHandler() {
        return new RestTemplateResponseErrorHandler();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateResponseErrorHandler restTemplateResponseErrorHandler) {
        return new RestTemplateBuilder()
                .errorHandler(restTemplateResponseErrorHandler)
                .setConnectTimeout(Duration.ofMillis(offerClientConfigProperties.connectionTimeout()))
                .setReadTimeout(Duration.ofMillis(offerClientConfigProperties.readTimeout()))
                .build();
    }

    @Bean
    public OfferFetchable offerFetchableClient(RestTemplate restTemplate ) {
        return new OfferRestTemplateClient(restTemplate, offerClientConfigProperties.uri(), offerClientConfigProperties.port());
    }


}
