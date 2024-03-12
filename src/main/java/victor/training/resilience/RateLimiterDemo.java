package victor.training.resilience;//package victor.training.resilience.client.reactive;

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
public class RateLimiterDemo {
  private final RateLimiterRegistry rateLimiterRegistry;

  @GetMapping("rate")
  @RateLimiter(name="rate1") // #1 AOP alternative
  public String rate() {
    return protectedCall();
  }

  @GetMapping("rate-fp")
  public String rateFP() { // #2 FP alternative
    return rateLimiterRegistry.rateLimiter("rate1")
        .executeSupplier(this::protectedCall);
  }

  @SneakyThrows
  private String protectedCall() {
    log.info("CALL-START");
    Thread.sleep(1000); // Imagine : webClient...retrieve(); restClient; restTemplate; SOAP client;
    log.info("CALL-END");
    return "rate-limited-call";
  }

  @PostConstruct
  public void init() {
    rateLimiterRegistry.rateLimiter("rate1").getEventPublisher()
        .onEvent(event -> log.info("RateLimiterEvent: " + event));
  }
}
