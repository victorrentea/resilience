//package victor.training.resilience.client.reactive;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import reactor.netty.http.client.HttpClientRequest;
//
//import java.time.Duration;
//
//import static java.util.Objects.requireNonNull;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//public class TimeoutReactiveApi {
//  private final WebClient webClient;
//  private final CacheManager cacheManager;
//
//  @GetMapping("timeout")
//  public Mono<String> timeout() {
//    Cache cache = requireNonNull(cacheManager.getCache("previous-response"));
//    return webClient.get().uri("http://localhost:8081/timeout")
//        .httpRequest(httpRequest -> {
//          HttpClientRequest nativeRequest = httpRequest.getNativeRequest();
//          nativeRequest.responseTimeout(Duration.ofMillis(5000)); // READ timeout, default = infinite
//          // - too short => requests taking longer that 500ms can succeed, but client sees errors
//          // - too long => keep your resources blocked + longer response to YOUR clients
//          // connect timeout = handshake 3ms. set it via HttpClient
//        })
//        .retrieve()
//        .bodyToMono(String.class)
//
//        // fallback to a cache
//        .doOnNext(log::info)
//        .doOnNext(e -> cache.put("previous", e))
//
//        .doOnError(e -> log.error("Error: " + e.getMessage(), e))
//        .onErrorResume(Exception.class::isInstance,
//            e -> Mono.justOrEmpty(cache.get("previous", String.class))
//                .switchIfEmpty(Mono.error(e)))
//
//        ;
//
//  }
//}
