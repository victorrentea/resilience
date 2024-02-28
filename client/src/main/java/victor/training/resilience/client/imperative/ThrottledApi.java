package victor.training.resilience.client.imperative;//package victor.training.resilience.client.reactive;

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
  private final BulkheadRegistry bulkheadRegistry;

  @GetMapping("throttled") // throttling = limit the number of concurrent requests
//  @Bulkhead(name = "bulkhead")
  public String throttled() {
    var globalBulkhead = bulkheadRegistry.bulkhead("bulkhead");
    return globalBulkhead.executeSupplier(() -> {
      log.info("CALL-START");
      String r = protectedCall();
      log.info("CALL-END");
      return r;
    });
  }

  @GetMapping("throttled-tenant")
  public String throttledTenant() {
    int tenantId = new Random().nextInt(2);
    // Imagine tenant-id comes from:
    // - @RequestHeader("x-tenant-id")
    // - client-api-key
    // - requestDto.region, ...

    var perTenantBulkhead = bulkheadRegistry.bulkhead("bulkhead-" + tenantId);

    return perTenantBulkhead.executeSupplier(() -> {
      log.info("CALL-START");
      String r = protectedCall();
      log.info("CALL-END");
      return r;
    });
  }

  @SneakyThrows
  private String protectedCall() {
    // Imagine here:
    // - webClient...retrieve()
    // - repo.find/insert
    // - redis, kafka, mongo...
    Thread.sleep(1000);
    return "throttled-call";
  }
}
