package victor.training.resilience.blocking;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CircuitBreakerDemo {
  private final RestClient rest;
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  @Value("${target.url.base}")
  private final String base;

  @GetMapping("circuit")
  @CircuitBreaker(name = "circuit1") // #1 AOP alternative
  public String circuitAOP() {
    return call();
  }

  @GetMapping("circuit-fp")
  public String circuitFP() { // #2 FP alternative
    return circuitBreakerRegistry.circuitBreaker("circuit1")
        .executeSupplier(this::call);
  }

  private String call() {
    log.info("CALL-START");
    String r = rest.get()
        .uri(base + "/circuit-api")
        .retrieve()
        .body(String.class);
    log.info("CALL-END");
    return r;
  }
}
