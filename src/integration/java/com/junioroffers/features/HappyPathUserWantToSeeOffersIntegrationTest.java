package com.junioroffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.junioroffers.BaseIntegrationTest;
import com.junioroffers.SampleJobOffersResponse;
import com.junioroffers.domain.offer.OfferFacade;
import com.junioroffers.domain.offer.OfferFetchable;
import com.junioroffers.domain.offer.dto.JobOfferResponse;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;

public class HappyPathUserWantToSeeOffersIntegrationTest extends BaseIntegrationTest implements SampleJobOffersResponse {

    @Autowired
    OfferFetchable offerRestTemplateClient;
    @Autowired
    OfferFacade offerFacade;


    @Test
    public void user_fetch_offers_happy_path_test() throws Exception {

//        1: there are no offers in external HTTP server (http://ec2-3-120-147-150.eu-central-1.compute.amazonaws.com:5057/offers)
        // given & when & then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));
       // List<JobOfferResponse> jobOfferResponses = offerRestTemplateClient.fetchOffers();


//        2: scheduler ran 1st time and made GET to external server and system added 0 offers to database
        // given & when
        List<OfferResponseDto> offerResponseDtos = offerFacade.fetchAllOffersAndSaveAllIfNotExists();
        // then
        Assertions.assertThat(offerResponseDtos).isEmpty();


            //f9 i puszczamy test dalej
//        3: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned UNAUTHORIZED(401)
//        4: user made GET /offers with no jwt token and system returned UNAUTHORIZED(401)
//        5: user made POST /register with username=someUser, password=somePassword and system registered user with status OK(200)
//        6: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC


//        7: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 0 offers
        // given & when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get("/offers")
                .contentType(MediaType.APPLICATION_JSON));
        // then
        MvcResult mvcResult = perform.andExpect((status().isOk())).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List <OfferResponseDto> response = objectMapper.readValue(contentAsString, new TypeReference<>(){});
        Assertions.assertThat(response).isEmpty();


//        8: there are 2 new offers in external HTTP server
//        9: scheduler ran 2nd time and made GET to external server and system added 2 new offers with ids: 1000 and 2000 to database
//        10: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with ids: 1000 and 2000
//        11: user made GET /offers/9999 and system returned NOT_FOUND(404) with message “Offer with id 9999 not found”
//        12: user made GET /offers/1000 and system returned OK(200) with offer
//        13: there are 2 new offers in external HTTP server
//        14: scheduler ran 3rd time and made GET to external server and system added 2 new offers with ids: 3000 and 4000 to database
//        15: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 4 offers with ids: 1000,2000, 3000 and 4000

    }
}
