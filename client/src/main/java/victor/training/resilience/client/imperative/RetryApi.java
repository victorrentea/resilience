
package victor.training.resilience.client.imperative;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Observed
@Slf4j
public class RetryApi {
  private final RestClient restClient;
  private final Retry retry;
  private final AtomicInteger counter = new AtomicInteger(1);

  public RetryApi(RetryRegistry retryRegistry, RestClient restClient) {
    this.restClient = restClient;
    retry = retryRegistry.retry("retry");
    retry.getEventPublisher().onEvent(event -> log.info("RetryEvent: " + event));
  }

  @GetMapping("retry")
//  @Retry("retry") // AOP
  public String retry() {
    return retry.executeSupplier(() -> {
      log.info("Call #{}", counter.incrementAndGet());
      String r = restClient.get().uri("http://localhost:8081/fail-half")
          .retrieve()
          .body(String.class);
      log.info("OK");
      return r
          ;
    });
  }

}
