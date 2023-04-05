package com.junioroffers.domain.offer;

import com.junioroffers.domain.offer.dto.JobOfferResponse;
import com.junioroffers.domain.offer.dto.OfferRequestDto;
import com.junioroffers.domain.offer.dto.OfferResponseDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

class OfferFacadeTest {


    @Test
    void should_save_four_offers_when_no_offers_in_database(){
        // given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration(List.of()).offerFacadeForTests();
        // when
        offerFacade.saveOffer(new OfferRequestDto("LuX", "JJD", "3000", "1"));
        offerFacade.saveOffer(new OfferRequestDto("BIGbob", "JMD", "6000", "2"));
        offerFacade.saveOffer(new OfferRequestDto("Stellers", "JSD", "9000", "3"));
        offerFacade.saveOffer(new OfferRequestDto("StudentHunt", "CEO", "50000", "4"));
        // then
        assertThat(offerFacade.findAllOffers()).hasSize(4);
    }

    @Test
    void should_save_only_two_offers_when_repository_had_four_added_with_offer_urls(){
        // given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration(
                List.of(
                        new JobOfferResponse("title", "id", "5000", "1"),
                        new JobOfferResponse("title", "id", "5000", "2"),
                        new JobOfferResponse("title", "id", "6000", "3"),
                        new JobOfferResponse("title", "id", "3000", "4"),
                        new JobOfferResponse("Junior", "Comarch", "11000", "https://exemplar.pl/5"),
                        new JobOfferResponse("Mid", "Finanteq", "14000", "https://otherexemplar.pl/6")
                )
        ).offerFacadeForTests();
        offerFacade.saveOffer(new OfferRequestDto("id", "position", "5000", "1"));
        offerFacade.saveOffer(new OfferRequestDto("id", "position", "5000", "2"));
        offerFacade.saveOffer(new OfferRequestDto("id", "position", "6000", "3"));
        offerFacade.saveOffer(new OfferRequestDto("id", "position", "3000", "4"));
        assertThat(offerFacade.findAllOffers()).hasSize(4);

        // when
        List<OfferResponseDto> response = offerFacade.fetchAllOffersAndSaveAllIfNotExists();

        // then
        assertThat(List.of(
                        response.get(0).offerUrl(),
                        response.get(1).offerUrl()
                )
        ).containsExactlyInAnyOrder("https://exemplar.pl/5", "https://otherexemplar.pl/6");
    }

    @Test
    void should_throw_duplicate_key_exception_when_with_offer_url_exist(){
        //Given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration(List.of()).offerFacadeForTests();
        OfferResponseDto offerResponseDto = offerFacade.saveOffer(new OfferRequestDto("BigCompany", "junior", "2999", "www.bigcompany.pl"));
        assertThat(offerFacade.findOfferById(offerResponseDto.id()).id()).isEqualTo(offerResponseDto.id());
        //When
        Throwable throwed = catchThrowable(()-> offerFacade.saveOffer(new OfferRequestDto("Company", "mid", "4999", "www.bigcompany.pl")));
        //Then
        AssertionsForClassTypes.assertThat(throwed)
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessage("Offer with offerUrl [www.bigcompany.pl] already exists");
    }

    @Test
    void should_throw_not_found_exception_when_offer_not_found(){
        // given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration(List.of()).offerFacadeForTests();
        assertThat(offerFacade.findAllOffers()).isEmpty();
        // when
        Throwable thrown = catchThrowable(() -> offerFacade.findOfferById("100"));
        // then
        AssertionsForClassTypes.assertThat(thrown)
                .isInstanceOf(OfferNotFoundException.class)
                .hasMessage("Offer with id 100 not found");
    }

    @Test
    void should_fetch_from_jobs_from_remote_and_save_all_offers_when_repository_is_empty(){
        //Given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration().offerFacadeForTests(); //default hardcoded in config class
        assertThat(offerFacade.findAllOffers()).isEmpty();
        //When
        List<OfferResponseDto> offerResponseDtos = offerFacade.fetchAllOffersAndSaveAllIfNotExists();
        //Then
        assertEquals(6, offerResponseDtos.size());
        assertEquals("id", offerResponseDtos.get(4).companyName());
    }

    @Test
    void should_find_offer_by_id_when_offer_was_saved(){
        //Given
        OfferFacade offerFacade = new OfferFacadeTestConfiguration().offerFacadeForTests();
        assertThat(offerFacade.findAllOffers()).isEmpty();
        OfferResponseDto offerResponseDto = offerFacade.saveOffer(new OfferRequestDto("FatFish", "HR", "20000", "url.pl"));
        //When
        OfferResponseDto offerById = offerFacade.findOfferById(offerResponseDto.id());
        //Then
        assertNotNull(offerById);
        assertThat(offerById).isEqualTo(OfferResponseDto.builder()
                .id(offerResponseDto.id())
                .companyName("FatFish")
                .position("HR")
                .salary("20000")
                .offerUrl("url.pl")
                .build()
        );
    }

}