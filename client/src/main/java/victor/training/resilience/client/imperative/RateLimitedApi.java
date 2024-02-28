package victor.training.resilience.client.imperative;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Duration.ofSeconds;

@RequiredArgsConstructor
@RestController
@Slf4j
public class RateLimitedApi {
  private final RateLimiterRegistry rateLimiterRegistry;

  @GetMapping("rate")
  public String rate() {
    return rateLimiterRegistry.rateLimiter("rate")
        .executeSupplier(() -> protectedCall());
  }

  @SneakyThrows
  private String protectedCall() {
    // Imagine here: webClient...retrieve()
    log.info("CALL-START");
    Thread.sleep(1000);
    log.info("CALL-END");
    return "rate-limited-call";
  }
}
