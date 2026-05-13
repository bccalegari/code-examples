package br.com.example.clientapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ClientApiController {
    private final ProviderApiService providerApiService;

    public ClientApiController(ProviderApiService providerApiService) {
        this.providerApiService = providerApiService;
    }

    @GetMapping
    public ResponseEntity<String> getData(
            @RequestHeader(value = "X-Scenario", defaultValue = "success") String scenario,
            @RequestHeader(value = "X-Use-Circuit-Breaker", defaultValue = "true") boolean useCircuitBreaker
    ) {
        if (useCircuitBreaker) {
            return ResponseEntity.ok(providerApiService.getDataWithCircuitBreaker(scenario));
        }

        return ResponseEntity.ok(providerApiService.getDataWithoutCircuitBreaker(scenario));
    }
}