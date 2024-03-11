package victor.training.resilience.client.blocking;//package victor.training.resilience.client.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TimeoutApi {
  private final RestClient restClient;
  private final CacheManager cacheManager;

  @GetMapping("timeout")
  public String timeout() {
    Cache cache = requireNonNull(cacheManager.getCache("previous-response"));
    try {
      String r = restClient.get().uri("http://localhost:8081/timeout")
          .retrieve()
          .body(String.class);
      log.info("Got {}", r);
      cache.put("previous", r);
      return r;
    } catch (Exception e) {
      String r = cache.get("previous", String.class);
      if (r == null) {
        log.error("Failure could not be recovered from cache", e);
        throw new RuntimeException("Unable to recover from cache: empty", e);
      }
      log.error("Recovered failure from cache", e);
      return r + " (from cache)";
    }
  }
}
