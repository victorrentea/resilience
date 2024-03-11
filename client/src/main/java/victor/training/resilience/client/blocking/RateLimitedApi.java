package victor.training.resilience.client.blocking;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
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
  @RateLimiter(name="rate") // #1 AOP alternative
  public String rate() {
    return protectedCall();
  }

  public String rateFP() { // #2 FP alternative
    return rateLimiterRegistry.rateLimiter("rate")
        .executeSupplier(this::protectedCall);
  }

  @PostConstruct
  public void init() {
    rateLimiterRegistry.rateLimiter("rate").getEventPublisher()
        .onEvent(event -> log.info("RateLimiterEvent failed: " + event));
  }

  @SneakyThrows
  private String protectedCall() {
    log.info("CALL-START");
    // Imagine here: webClient...retrieve(); restClient; restTemplate; SOAP client;
    Thread.sleep(1000);
    log.info("CALL-END");
    return "rate-limited-call";
  }
}