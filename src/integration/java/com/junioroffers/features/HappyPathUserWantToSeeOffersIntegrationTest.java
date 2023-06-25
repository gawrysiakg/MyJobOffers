package com.junioroffers.features;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.junioroffers.BaseIntegrationTest;
import com.junioroffers.SampleJobOffersResponse;
import com.junioroffers.domain.loginandregister.dto.RegistrationResultDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import com.junioroffers.infrastructure.loginandregister.controller.dto.JwtResponseDto;
import com.junioroffers.infrastructure.offer.scheduler.FetchOffersScheduler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HappyPathUserWantToSeeOffersIntegrationTest extends BaseIntegrationTest implements SampleJobOffersResponse {

    @Autowired
    FetchOffersScheduler fetchOffersScheduler;

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

//    @RegisterExtension
//    public static WireMockExtension wireMockServer = WireMockExtension.newInstance()
//            .options(wireMockConfig().dynamicPort())
//            .build();

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("myjoboffers.offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("myjoboffers.offer.http.client.config.port", () -> wireMockServer.getPort());
    }

    @Test
    public void user_fetch_offers_happy_path_test() throws Exception {

//        1: there are no offers in external HTTP server (http://ec2-3-120-147-150.eu-central-1.compute.amazonaws.com:5057/offers)
        // given & when & then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithZeroOffersJson())));


//        2: scheduler ran 1st time and made GET to external server and system added 0 offers to database
        // given & when
        List<OfferResponseDto> offerResponseDtos = fetchOffersScheduler.fetchOffers();
        // then
        Assertions.assertThat(offerResponseDtos).isEmpty();


//        3: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned UNAUTHORIZED(401)
        // given & when
        ResultActions failedLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        failedLoginRequest
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("""
                        {
                          "message": "Bad Credentials",
                          "status": "UNAUTHORIZED"
                        }
                        """.trim()));


//        4: user made GET /offers with no jwt token and system returned UNAUTHORIZED(401)
        // given & when
        ResultActions failedGetOffersRequest = mockMvc.perform(MockMvcRequestBuilders.get("/offers")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        failedGetOffersRequest.andExpect(status().isForbidden());


//        5: user made POST /register with username=someUser, password=somePassword and system registered user with status OK(200)
        // given & when
        ResultActions registerAction = mockMvc.perform(post("/register")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult registerActionResult = registerAction.andExpect(status().isCreated()).andReturn();
        String registerActionResultJson = registerActionResult.getResponse().getContentAsString();
        RegistrationResultDto registrationResultDto = objectMapper.readValue(registerActionResultJson, RegistrationResultDto.class);
        assertAll(
                () -> assertThat(registrationResultDto.username()).isEqualTo("someUser"),
                () -> assertThat(registrationResultDto.created()).isTrue(),
                () -> assertThat(registrationResultDto.id()).isNotNull()
        );


//        6: user tried to get JWT token by requesting POST /token with username=someUser, password=somePassword and system returned OK(200) and jwttoken=AAAA.BBBB.CCC
        // given & when
        ResultActions successLoginRequest = mockMvc.perform(post("/token")
                .content("""
                        {
                        "username": "someUser",
                        "password": "somePassword"
                        }
                        """.trim())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult mvcResult = successLoginRequest.andExpect(status().isOk()).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponse = objectMapper.readValue(json, JwtResponseDto.class);
        String token = jwtResponse.token();
        assertAll(
                () -> assertThat(jwtResponse.username()).isEqualTo("someUser"),
                () -> assertThat(token).matches(Pattern.compile("^([A-Za-z0-9-_=]+\\.)+([A-Za-z0-9-_=])+\\.?$"))
        );


//        7: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 0 offers
        // given
        String offersUrl = "/offers";
        // when
        ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.get(offersUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        );
        // then
        MvcResult mvcResult2 = perform.andExpect(status().isOk()).andReturn();
        String jsonWithOffers = mvcResult2.getResponse().getContentAsString();
        List<OfferResponseDto> offers = objectMapper.readValue(jsonWithOffers, new TypeReference<>() {
        });
        assertThat(offers).isEmpty();


//        8: there are 2 new offers in external HTTP server
        // given & when & then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithTwoOffersJson())));


//        9: scheduler ran 2nd time and made GET to external server and system added 2 new offers with ids: 1000 and 2000 to database
        // given & when
        List<OfferResponseDto> expectedFetchedTwoOffers = fetchOffersScheduler.fetchOffers();
        // then
        assertThat(expectedFetchedTwoOffers).hasSize(2);


//        10: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 2 offers with ids: 1000 and 2000
        // given & when
        ResultActions userExpectsTwoOffers = mockMvc.perform(MockMvcRequestBuilders.get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        // then
        MvcResult expectedTwoOffers = userExpectsTwoOffers.andExpect((status().isOk())).andReturn();
        String expectedAsString = expectedTwoOffers.getResponse().getContentAsString();
        List <OfferResponseDto> responseExpectedTwo = objectMapper.readValue(expectedAsString, new TypeReference<>(){});
        Assertions.assertThat(responseExpectedTwo).hasSize(2);
        OfferResponseDto expectedFirstOffer = responseExpectedTwo.get(0);
        OfferResponseDto expectedSecondOffer = responseExpectedTwo.get(1);
        assertThat(responseExpectedTwo).containsExactlyInAnyOrder(
                new OfferResponseDto(expectedFirstOffer.id(), expectedFirstOffer.companyName(), expectedFirstOffer.position(), expectedFirstOffer.salary(), expectedFirstOffer.offerUrl()),
                new OfferResponseDto(expectedSecondOffer.id(), expectedSecondOffer.companyName(), expectedSecondOffer.position(), expectedSecondOffer.salary(), expectedSecondOffer.offerUrl())
        );



//        11: user made GET /offers/9999 and system returned NOT_FOUND(404) with message “Offer with id 9999 not found”
        // given & when
        ResultActions performForNotFoundId = mockMvc.perform(MockMvcRequestBuilders.get("/offers/9999")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        // then
        performForNotFoundId.andExpect(status().isNotFound())
                .andExpect(content().json("""
                        {
                        "message": "Offer with id 9999 not found",
                        "status": "NOT_FOUND"
                        }
                        """.trim()
                ));


//        12: user made GET /offers/1000 and system returned OK(200) with offer
        // given
        String idOfferAddedToDb = expectedFirstOffer.id();
        //when
        ResultActions getSavedOfferById = mockMvc.perform(MockMvcRequestBuilders.get("/offers/"+idOfferAddedToDb)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        String expectedOffer = getSavedOfferById.andExpect(status().is(200)).andReturn().getResponse().getContentAsString();
        OfferResponseDto offerReturnedFromDb = objectMapper.readValue(expectedOffer, OfferResponseDto.class);
        // then
        assertThat(offerReturnedFromDb.id()).isEqualTo(idOfferAddedToDb);
        assertThat(offerReturnedFromDb).isEqualTo(expectedFirstOffer);


//        13: there are 2 new offers in external HTTP server
        // given & when & then
        wireMockServer.stubFor(WireMock.get("/offers")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyWithFourOffersJson())));


//        14: scheduler ran 3rd time and made GET to external server and system added 2 new offers with ids: 3000 and 4000 to database
        // given & when
        List<OfferResponseDto> expectedFetchedOnlyTwoOffersNotExistedInDb = fetchOffersScheduler.fetchOffers();
        // then
        assertThat(expectedFetchedOnlyTwoOffersNotExistedInDb).hasSize(2);


//        15: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 4 offers with ids: 1000,2000, 3000 and 4000
        // given & when
        ResultActions userExpectsFourOffers = mockMvc.perform(MockMvcRequestBuilders.get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        // then
        MvcResult expectedFourOffers = userExpectsFourOffers.andExpect((status().isOk())).andReturn();
        String expectedFourAsString = expectedFourOffers.getResponse().getContentAsString();
        List <OfferResponseDto> responseExpectedFour = objectMapper.readValue(expectedFourAsString, new TypeReference<>(){});
        Assertions.assertThat(responseExpectedFour).hasSize(4);
        OfferResponseDto expectedFirstFromFourOffer = responseExpectedFour.get(0);
        OfferResponseDto expectedSecondFromFourOffer = responseExpectedFour.get(1);
        OfferResponseDto expectedThirdFromFourOffer = responseExpectedFour.get(2);
        OfferResponseDto expectedFourthFromFourOffer = responseExpectedFour.get(3);
        assertThat(responseExpectedFour).containsExactlyInAnyOrder(
                new OfferResponseDto(expectedFirstFromFourOffer.id(), expectedFirstFromFourOffer.companyName(), expectedFirstFromFourOffer.position(), expectedFirstFromFourOffer.salary(), expectedFirstFromFourOffer.offerUrl()),
                new OfferResponseDto(expectedSecondFromFourOffer.id(), expectedSecondFromFourOffer.companyName(), expectedSecondFromFourOffer.position(), expectedSecondFromFourOffer.salary(), expectedSecondFromFourOffer.offerUrl()),
                new OfferResponseDto(expectedThirdFromFourOffer.id(), expectedThirdFromFourOffer.companyName(), expectedThirdFromFourOffer.position(), expectedThirdFromFourOffer.salary(), expectedThirdFromFourOffer.offerUrl()),
                new OfferResponseDto(expectedFourthFromFourOffer.id(), expectedFourthFromFourOffer.companyName(), expectedFourthFromFourOffer.position(), expectedFourthFromFourOffer.salary(), expectedFourthFromFourOffer.offerUrl())
        );



//        16: user made POST /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and offer as body and system returned CREATED(201) with saved offer
        //test check 405 error - method not allowed (nie ma tej metody)
        // given
        // when
        ResultActions performPostOffersWithOneOffer  = mockMvc.perform(post("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "companyName": "someCompany",
                        "position": "somePosition",
                        "salary": "7 000 - 9 000 PLN",
                        "offerUrl": "https://newoffers.pl/offer/12346"
                        }
                        """.trim()));
        // then
        String contentAsString1 = performPostOffersWithOneOffer.andReturn().getResponse().getContentAsString();
        OfferResponseDto offerResponseDto = objectMapper.readValue(contentAsString1, OfferResponseDto.class);
        String id = offerResponseDto.id();

        assertAll(
                () -> assertThat(offerResponseDto.companyName()).isEqualTo("someCompany"),
                () -> assertThat(offerResponseDto.position()).isEqualTo("somePosition"),
                () -> assertThat(offerResponseDto.offerUrl()).isEqualTo("https://newoffers.pl/offer/12346"),
                () -> assertThat(id).isNotNull()
        );


//       17: user made GET /offers with header “Authorization: Bearer AAAA.BBBB.CCC” and system returned OK(200) with 1 offer
//      given & when
        ResultActions performGet = mockMvc.perform(MockMvcRequestBuilders.get("/offers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        // then
        String oneOfferJson= performGet.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List <OfferResponseDto> offerResponseDtoList = objectMapper.readValue(oneOfferJson, new TypeReference<>() {});
        assertThat(offerResponseDtoList).hasSize(5);
        assertThat(offerResponseDtoList.stream().map(OfferResponseDto::id)).contains(id);
    }
}
