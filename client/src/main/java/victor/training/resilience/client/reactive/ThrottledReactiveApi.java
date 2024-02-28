//package victor.training.resilience.client.reactive;
//
//import io.github.resilience4j.bulkhead.Bulkhead;
//import io.github.resilience4j.bulkhead.BulkheadRegistry;
//import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//import java.util.Random;
//
//import static java.time.Duration.ofSeconds;
//
//@RequiredArgsConstructor
//@RestController
//@Slf4j
//public class ThrottledReactiveApi {
//  private final BulkheadRegistry bulkheadRegistry;
//
//  @GetMapping("throttled") // throttling = limit the volume of concurrent requests
//  public Mono<String> throttled() {
//    Bulkhead globalBulkhead = bulkheadRegistry.bulkhead("bulkhead");
//    return protectedCall()
//        .doOnSubscribe(__ -> log.info("CALL-START"))
//        .doOnNext(__ -> log.info("CALL-END"))
//        .transformDeferred(BulkheadOperator.of(globalBulkhead))
//        .log("ENTRY")
//        ;
//  }
//
//  @GetMapping("throttled-tenant")
//  public Mono<String> throttledTenant() {
//    int tenantId = new Random().nextInt(2);
//    // Imagine tenant-id comes from:
//    // - @RequestHeader("x-tenant-id")
//    // - client-api-key
//    // - requestDto.region, ...
//
//    Bulkhead perTenantBulkhead = bulkheadRegistry.bulkhead("bulkhead-" + tenantId);
//    return protectedCall()
//
//        .doOnSubscribe(__ -> log.info("CALL-START: " + tenantId))
//        .doOnNext(__ -> log.info("CALL-END: " + tenantId))
//        .transformDeferred(BulkheadOperator.of(perTenantBulkhead))
//        // or, getting the tenant-id from Reactor Context
//        // .transformDeferredContextual((m,context) -> BulkheadOperator.<String>of(bulkheadRegistry.bulkhead("bulkhead-" + context.get("tenant-id"))).apply(m))
//        .log("ENTRY")
//        ;
//  }
//  private Mono<String> protectedCall() {
//    // Imagine here:
//    // - webClient...retrieve()
//    // - repo.find/insert
//    // - redis, kafka, mongo...
//    return Mono.just("throttled-call")
//        .delayElement(ofSeconds(1));
//  }
//}
