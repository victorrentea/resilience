
package victor.training.resilience.client;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.MaxRetriesExceededException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;

import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@RestController
@Observed
@Slf4j
public class RetryApi {
  private final RetryRegistry retryRegistry;
  private final WebClient webClient;

  @GetMapping("retry")
  public Mono<String> retry() {
    Retry retry = retryRegistry.retry("retry");
    return webClient.get().uri("http://localhost:8081/fail-half")
        .retrieve()
        .bodyToMono(String.class)
        .log("BEFORE_CALL")
        .transformDeferred(RetryOperator.of(retry))
        .log("ENTRY")
        ;
  }

}
