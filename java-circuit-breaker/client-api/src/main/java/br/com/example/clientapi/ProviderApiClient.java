package br.com.example.clientapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ProviderApiClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProviderApiClient(
            RestTemplate restTemplate,
            @Value("${provider.api.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String getProviderData(String scenarioHeader) {
        var headers = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("X-Scenario", scenarioHeader)));

        return restTemplate
                .exchange(baseUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class)
                .getBody();
    }
}
