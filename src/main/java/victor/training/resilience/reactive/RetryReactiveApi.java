//
//package victor.training.resilience.client.reactive;
//
//import io.github.resilience4j.reactor.retry.RetryOperator;
//import io.github.resilience4j.retry.Retry;
//import io.github.resilience4j.retry.RetryRegistry;
//import io.micrometer.observation.annotation.Observed;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import static java.time.Duration.ofSeconds;
//import static java.util.Objects.requireNonNull;
//
//@RestController
//@Observed
//@Slf4j
//public class RetryReactiveApi {
//  private final WebClient webClient;
//  private Retry retry;
//
//  public RetryReactiveApi(RetryRegistry retryRegistry, WebClient webClient) {
//    this.webClient = webClient;
//    retry = retryRegistry.retry("retry");
//    retry.getEventPublisher().onEvent(event -> log.info("RetryEvent: " + event));
//  }
//
//  @GetMapping("retry")
//  public Mono<String> retry() {
//    return webClient.get().uri("http://localhost:8081/fail-half")
//        .retrieve()
//        .bodyToMono(String.class)
//        .log("BEFORE_CALL")
//        .transformDeferred(RetryOperator.of(retry))
//        .log("ENTRY")
//        ;
//  }
//
//}
