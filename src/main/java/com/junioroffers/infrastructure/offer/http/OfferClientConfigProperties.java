package com.junioroffers.infrastructure.offer.http;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@ConfigurationProperties(prefix = "myjoboffers.offer.http.client.config")
public record OfferClientConfigProperties(long connectionTimeout, long readTimeout, String uri, int port) {
}
