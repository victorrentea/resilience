package victor.training.resilience.client.blocking;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CircuitApi {
  private final RestClient restClient;
  private final CircuitBreakerRegistry circuitBreakerRegistry;

  @GetMapping("circuit")
//  @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker()
  public String circuit() {
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit");
//    circuitBreaker.getEventPublisher().onEvent(new EventConsumer<CircuitBreakerEvent>() {
//      @Override
//      public void consumeEvent(CircuitBreakerEvent event) {
//        event
//      }
//    });
    return circuitBreaker.executeSupplier(() ->
        restClient.get()
            .uri("http://localhost:8081/fail-half")
//        .uri("http://localhost:8081/ok")
            .retrieve()
            .body(String.class));
  }
}
