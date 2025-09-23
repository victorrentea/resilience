
package victor.training.resilience;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@RestController
@Observed
@Slf4j
@RequiredArgsConstructor
public class RetryDemo {
  private final RestClient rest;
  @Value("${target.url.base}")
  private final String base;

  @GetMapping("retry")
  @Retry(name = "retry1") // #1 AOP
  public String retryAOP() {
    return performCall();
  }

  private final RetryRegistry retryRegistry;
  @GetMapping("retry-fp")
  public String retryFP() { // #2 FP, less magic
//    retryRegistry... TODO
    return performCall();
  }

  private final AtomicInteger counter = new AtomicInteger(1);
  private String performCall() {
    log.info("Call no. {}", counter.incrementAndGet());
    String r = rest.get().uri(base+"/retry-api") //below
        .retrieve()
        .body(String.class);
    log.info("OK");
    return r;
  }

  @PostConstruct
  public void init() {
    retryRegistry.retry("retry1").getEventPublisher().onEvent(event -> log.info("RetryEvent: " + event));
  }
  public static class RetryPredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
      System.out.println("Check error: " + throwable);
      return false;
    }
  }

  // ============ for further experiments w/o @Test =============

  @GetMapping("retry-api")
  public ResponseEntity<String> retryApi() {
    if (Math.random() < .5) {
      log.info("Remote returns OK");
      return ResponseEntity.ok("OK");
    } else {
      log.info("Remote returns FAILURE");
//      Thread.sleep(5000); // to cause a timeout
      return ResponseEntity.status(400).body("Bad Request - don't retry");
//      return ResponseEntity.status(503).body("Service Unavailable - retry");
//      return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body("{\"retryable\": true}");
    }
  }
}
