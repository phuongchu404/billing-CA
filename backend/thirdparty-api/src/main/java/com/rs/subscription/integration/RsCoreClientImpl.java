package com.rs.subscription.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
@Profile("!test")
@Slf4j
public class RsCoreClientImpl implements RsCoreClient {

    private final RestClient restClient;

    public RsCoreClientImpl(
        @Value("${rs.core.base-url:http://localhost:8081}") String baseUrl,
        @Value("${rs.core.service-token:dev-token}") String serviceToken
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public CertificateProvisioningResult createCertificate(CertificateRequest request) {
        log.info("Calling RS Core to provision certificate for user={}, subscription={}",
            request.getUserId(), request.getSubscriptionId());
        return restClient.post()
            .uri("/internal/certificates")
            .body(request)
            .retrieve()
            .body(CertificateProvisioningResult.class);
    }
}
