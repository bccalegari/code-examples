package br.com.example.providerapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
public class ProviderApiController {

    @GetMapping
    public ResponseEntity<String> getProviderData(
            @RequestHeader(value = "X-Scenario", defaultValue = "success") String scenario
    ) {
        return switch (scenario) {
            case "error" -> ResponseEntity.status(500).body("Internal Server Error");
            case "timeout" -> {
                sleep(5000);
                yield ResponseEntity.ok("Data from provider after delay");
            }
            case "random" -> {
                if (Math.random() < 0.5) {
                    yield ResponseEntity.ok("Data from provider");
                }
                yield ResponseEntity.status(500).body("Internal Server Error");

            }
            default -> ResponseEntity.ok("Data from provider");
        };
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
