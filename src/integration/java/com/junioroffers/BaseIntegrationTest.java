package com.junioroffers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ActiveProfiles("integration")
@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(classes= MyJobOffersSpringBootApplication.class)
public class BaseIntegrationTest {

    public static final String WIRE_MOCK_HOST = "http://localhost";

    //odczytuje json i mapuje na obiekty Javy (jak Gson) , jest ze springa
    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public MockMvc mockMvc;
 //   wireMock jest do testowania zależności zewnętrznych a mockMvc uderza do naszych endpointów
    // jest jeszcze testRestTemplate zamiast mockMvc

    //odpala bazę mongo w kontenerze, tylko na potrzeby testowe, potem baza umiera
    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
    //robimy wire mocka żeby mieć mocka a nie dane z zewnętrznego serwera, bo zewnętrzny serwer może nie odpowiadać,
    // albo długo odpowiadać a tak testy będą niezależne. Robimy serwer ze swojego komputera po localhost
    @RegisterExtension
    public static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    public static void propertyOverride(DynamicPropertyRegistry registry) {
       // registry.add("spring.data.mongodb.uri", ()->mongoDBContainer.getReplicaSetUrl()+"offers"); //doklei do nazwy test
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("myjoboffers.offer.http.client.config.uri", () -> WIRE_MOCK_HOST);
        registry.add("myjoboffers.offer.http.client.config.port", () -> wireMockServer.getPort());
    }

}
