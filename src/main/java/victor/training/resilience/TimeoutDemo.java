package victor.training.resilience;//package victor.training.resilience.client.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TimeoutDemo {
  private final RestClient restClient;
  private final CacheManager cacheManager;
  @Value("${target.url.base}")
  private final String base;

  @GetMapping("timeout")
  public String timeout() {
    Cache cache = Objects.requireNonNull(cacheManager.getCache("previous-response"));
    try {
      String r = restClient.get().uri(base + "/timeout-api") //below
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

  @GetMapping("timeout-api")
  public String timeoutApi() throws InterruptedException {
    if (Math.random() < .5) {
      return "fast";
    } else {
      Thread.sleep(3000);
      return "slow";
    }
  }

  @Configuration
  public static class RestClientConfig {
    @Bean
    public RestClient restClient() {
      RestTemplate restTemplate = new RestTemplate();
      var requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
      requestFactory.setConnectTimeout(20); // = TCP/IP handshake = time to wait for server to accept the connection
      requestFactory.setReadTimeout(30000); // aka 'response timeout' =
      // = waiting time in queue on server to get a thread to work the request on
      // + server processing time, including any API calls, DB ...
      // + serializing the request/response
      // + network transfer <->
      // until remote server closes the connection
      return RestClient.create(restTemplate);
    }
  }
}
