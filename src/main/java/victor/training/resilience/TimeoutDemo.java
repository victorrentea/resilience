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

@Slf4j
@RestController
@RequiredArgsConstructor
public class TimeoutDemo {
  private final RestClient restClient;
  @Value("${target.url.base}")
  private final String base;

  @GetMapping("timeout")
  public String timeout() {
    String r = restClient.get().uri(base + "/timeout-api") //below
        .retrieve()
        .body(String.class);
    log.info("Got {}", r);
    return r;
  }

  @Configuration
  public static class RestClientConfig {

    @Bean
    public RestClient restClient() {
      RestTemplate restTemplate = new RestTemplate();
      var requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
      // TODO set the response timeout to 2000 millis
      requestFactory.setConnectTimeout(20);
      requestFactory.setReadTimeout(2000);
      return RestClient.create(restTemplate);
    }
//    @Bean
//    public RestClient restClientADHD() { // z-gen
//      RestTemplate restTemplate = new RestTemplate();
//      var requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
//      requestFactory.setConnectTimeout(20);
//      requestFactory.setReadTimeout(500);
//      return RestClient.create(restTemplate);
//
//    }
  }


  // ============ for further experiments w/o @Test =============

  @GetMapping("timeout-api")
  public String timeoutApi() throws InterruptedException {
    if (Math.random() < .5) {
      return "fast";
    } else {
      Thread.sleep(3000);
      return "slow";
    }
  }
}
