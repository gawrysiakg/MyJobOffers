package com.junioroffers.http.error;

import com.junioroffers.domain.offer.OfferFetchable;
import com.junioroffers.infrastructure.offer.http.OfferClientConfig;
import com.junioroffers.infrastructure.offer.http.OfferRestTemplateClient;
import com.junioroffers.infrastructure.offer.http.RestTemplateResponseErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static com.junioroffers.BaseIntegrationTest.WIRE_MOCK_HOST;

@Component
public class OfferHttpClientTestConfig extends OfferClientConfig {

    public RestTemplateResponseErrorHandler restTemplateResponseErrorHandler() {
        return new RestTemplateResponseErrorHandler();
    }


    public OfferFetchable remoteOfferTestClient(int port, int connectionTimeout, int readTimeout) {
        final RestTemplate restTemplate = restTemplate(connectionTimeout, readTimeout, restTemplateResponseErrorHandler());
        return offerFetchableClient(restTemplate, WIRE_MOCK_HOST, port);
    }
}