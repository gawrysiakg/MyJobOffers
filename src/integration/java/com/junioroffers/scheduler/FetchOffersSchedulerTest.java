package com.junioroffers.scheduler;

import com.junioroffers.BaseIntegrationTest;
import com.junioroffers.MyJobOffersSpringBootApplication;
import com.junioroffers.domain.offer.OfferFetchable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(classes = MyJobOffersSpringBootApplication.class, properties = "scheduling.enabled=true")
public class FetchOffersSchedulerTest extends BaseIntegrationTest {

    @SpyBean
    OfferFetchable remoteOfferClient;


    @Test
    void should_fetch_offers_from_external_server_exactly_given_times(){

        await().
                atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(remoteOfferClient, times(1)).fetchOffers());
    }
}
