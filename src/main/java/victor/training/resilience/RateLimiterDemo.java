package victor.training.resilience;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static java.time.Duration.ofSeconds;

@RequiredArgsConstructor
@RestController
@Slf4j
public class RateLimiterDemo {
  private final RateLimiterRegistry rateLimiterRegistry;
  private final RateLimitedLogic rateLimitedLogic;

  @GetMapping("rate")
  @RateLimiter(name="rate1")
  public String rateLimiterAOP() { // AOP alternative
    return rateLimitedLogic.logic();
  }

  @GetMapping("rate-fp")
  public String global() {
    return rateLimiterRegistry.rateLimiter("rate1")
        .executeSupplier(rateLimitedLogic::logic);
  }

  @GetMapping("rate-per-tenant")
  public String perTenant(@RequestHeader String tenantId) {
    return rateLimiterRegistry.rateLimiter("rate-"+tenantId)
        .executeSupplier(rateLimitedLogic::logic);
  }
  @PostConstruct
  public void init() {
    rateLimiterRegistry.rateLimiter("rate1").getEventPublisher()
        .onEvent(event -> log.info("RateLimiterEvent: " + event));
  }
}

@Slf4j
@RequiredArgsConstructor
@Service
class RateLimitedLogic {
  public String logic() {
//    if (true) throw new RuntimeException("INTENTIONAL");
    // Imagine : webClient...retrieve(); restClient; restTemplate; SOAP client;
    return "OK";
  }
}