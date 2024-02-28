package victor.training.resilience.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Observed
@Slf4j
@SpringBootApplication
@RestController
@EnableFeignClients
@RequiredArgsConstructor
@EnableCaching
public class ClientApp {
  public static void main(String[] args) {
//    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(ClientApp.class, args);
  }

//  @Bean
//  public WebClient webClient() {
//    return WebClient.builder().build();
//  }

  @Bean
  public RestClient restClient() {
    RestTemplate restTemplate = new RestTemplate();
    ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(100);
    ((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setReadTimeout(2000);
    return RestClient.create(restTemplate);
  }


  private final CacheManager cacheManager;
  @EventListener(ApplicationStartedEvent.class)
  @Async
  public void checkCacheAutoEviction() throws InterruptedException {
    Cache cache = cacheManager.getCache("previous-response");
//    cache.clear();
    cache.put("k", "value");
    System.out.println(cache.get("k", String.class));
    Thread.sleep(4000);
    System.out.println(cache.get("k", String.class));
  }

}
