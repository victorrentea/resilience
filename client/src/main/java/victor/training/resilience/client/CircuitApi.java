package victor.training.resilience.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CircuitApi {
  private final WebClient webClient;
  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final CacheManager cacheManager;

  @GetMapping("circuit")
  public Mono<String> circuit() {
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit");
    return webClient.get().uri("http://localhost:8081/ok")
        .retrieve()
        .bodyToMono(String.class)
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        ;
  }
}
