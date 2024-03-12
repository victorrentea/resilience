package victor.training.resilience;//package victor.training.resilience.client.reactive;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequiredArgsConstructor
@RestController
@Slf4j
public class BulkheadDemo {

  @GetMapping("bulkhead") // throttling = limit the number of concurrent requests
  @Bulkhead(name = "bulkhead1") // #1 AOP alternative
  public String bulkhead() {
    return protectedCall();
  }

  private final BulkheadRegistry bulkheadRegistry;

  @GetMapping("bulkhead-fp") // throttling = limit the number of concurrent requests
  public String bulkheadFP() { // #2 FP alternative
    var globalBulkhead = bulkheadRegistry.bulkhead("bulkhead1");
    return globalBulkhead.executeSupplier(this::protectedCall);
  }

  @GetMapping("bulkhead-tenant")
  public String throttledTenant() {
    int tenantId = new Random().nextInt(2);
    // tenant-id could come from:
    // - @RequestHeader("x-tenant-id") String tenantId
    // - client-api-key from SecurityContextHolder.getContext().getAuthentication().getName();
    // - requestDto.region, ...
    var bulkheadPerTenant = bulkheadRegistry.bulkhead("bulkhead-" + tenantId);

    return bulkheadPerTenant.executeSupplier(this::protectedCall);
  }

  @SneakyThrows
  private String protectedCall() {
    log.info("CALL-START");
    Thread.sleep(5000); // REST, DB, SOAP, gRPC ...
    log.info("CALL-END");
    return "bulkhead-call";
  }

  @ExceptionHandler(BulkheadFullException.class) // can be made global in a @RestControllerAdvice
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // 503 Service Unavailable if global
//   @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) //429 Too Many Requests if limiting per-client
  public String onBulkhead() {
    return "Please try again later";
  }
}
