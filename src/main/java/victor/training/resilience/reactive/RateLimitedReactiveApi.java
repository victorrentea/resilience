//package victor.training.resilience.client.reactive;
//
//import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
//import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//import static java.time.Duration.ofSeconds;
//
//@RequiredArgsConstructor
//@RestController
//@Slf4j
//public class RateLimitedReactiveApi {
//  private final RateLimiterRegistry rateLimiterRegistry;
//
//  @GetMapping("rate")
//  public Mono<String> rate() {
//    return protectedCall()
//        .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter("rate")))
//        .log("ENTRY")
//        ;
//  }
//
//  private Mono<String> protectedCall() {
//    // Imagine here: webClient...retrieve()
//    return Mono.just("rate-limited-call")
//        .delayElement(ofSeconds(1))
//        .doOnSubscribe(__ -> log.info("CALL-START"))
//        .doOnNext(__ -> log.info("CALL-END"));
//  }
//}
