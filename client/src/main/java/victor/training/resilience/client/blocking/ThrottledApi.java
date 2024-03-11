package victor.training.resilience.client.blocking;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequiredArgsConstructor
@RestController
@Slf4j
public class ThrottledApi {
  @GetMapping("throttled") // throttling = limit the number of concurrent requests
  @Bulkhead(name = "bulkhead") // #1 AOP alternative
  public String throttled() {
    return protectedCall();
  }

  private final BulkheadRegistry bulkheadRegistry;
  public String throttledFP() { // #2 FP alternative
    var globalBulkhead = bulkheadRegistry.bulkhead("bulkhead");
    return globalBulkhead.executeSupplier(this::protectedCall);
  }

  @GetMapping("throttled-tenant")
  public String throttledTenant() {
    int tenantId = new Random().nextInt(2);
    // Imagine tenant-id comes from:
    // - @RequestHeader("x-tenant-id") String tenantId
    // - client-api-key from SecurityContextHolder.getContext().getAuthentication().getName();
    // - requestDto.region, ...
    var bulkheadPerTenant = bulkheadRegistry.bulkhead("bulkhead-" + tenantId);

    return bulkheadPerTenant.executeSupplier(this::protectedCall);
  }

  @SneakyThrows
  private String protectedCall() {
    log.info("CALL-START");
    Thread.sleep(10000);
    // Imagine here:
    // - webClient...retrieve()
    // - repo.find/insert
    // - redis, kafka, mongo...
    log.info("CALL-END");
    return "throttled-call";
  }
}
