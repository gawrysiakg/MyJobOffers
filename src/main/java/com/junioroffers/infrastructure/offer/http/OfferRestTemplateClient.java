package com.junioroffers.infrastructure.offer.http;

import com.junioroffers.domain.offer.OfferFetchable;
import com.junioroffers.domain.offer.dto.JobOfferResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@Log4j2
public class OfferRestTemplateClient implements OfferFetchable {

    // full url: http://ec2-3-120-147-150.eu-central-1.compute.amazonaws.com:5057/offers
    public static final String OFFER_SERVICE_PATH = "/offers";
    private final RestTemplate restTemplate;
    private final String uri;
    private final int port;



    @Override
    public List<JobOfferResponse> fetchOffers(){

        log.info("Started fetching job offers using http client");
        HttpHeaders headers = new HttpHeaders();
        final HttpEntity<HttpHeaders> requestEntity = new HttpEntity<>(headers);
        try {
            final ResponseEntity<List<JobOfferResponse>> response = makeGetRequest(requestEntity);
             List<JobOfferResponse> fetchedOffers = response.getBody();
             return fetchedOffers;
        } catch (ResourceAccessException e) {
                log.error("Error while fetching offers using http client: " + e.getMessage());
                return List.of();
        }
    }





    private ResponseEntity<List<JobOfferResponse>> makeGetRequest(HttpEntity<HttpHeaders> requestEntity) {
        final String url = UriComponentsBuilder.fromHttpUrl(getUrlForService(OFFER_SERVICE_PATH))
                .toUriString();
        ResponseEntity<List<JobOfferResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });
        return response;
    }




    private String getUrlForService(String service) {
        return uri + ":" + port + service;
    }
}
