package br.com.example.clientapi;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class ProviderApiService {
    private final ProviderApiClient providerApiClient;

    public ProviderApiService(ProviderApiClient providerApiClient) {
        this.providerApiClient = providerApiClient;
    }

    @CircuitBreaker(name = "providerApi", fallbackMethod = "fallbackGetData")
    public String getDataWithCircuitBreaker(String scenario) {
        return providerApiClient.getProviderData(scenario);
    }

    public String fallbackGetData(String scenario, Exception exception) {
        return "Fallback response for scenario: " + scenario + " due to: " + exception.getMessage();
    }

    public String getDataWithoutCircuitBreaker(String scenario) {
        return providerApiClient.getProviderData(scenario);
    }
}
