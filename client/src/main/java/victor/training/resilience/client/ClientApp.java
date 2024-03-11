package victor.training.resilience.client;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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
    var requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    requestFactory.setConnectTimeout(20); // = time to wait for TCP/IP handshake = time to wait for server to accept the connection
    requestFactory.setReadTimeout(30000); // aka 'response timeout'
    // = waiting time in queue on server to get a thread to work the request on
    // + server processing time (API calls, DB ...)👑👑👑
    // + serializing the response
    // + network transfer <->
    // until server closes the connection

    // to keep a bidirectional conn between client-server (eg chat)
    // we use WebSockets, Long polling (90s), Content-Type: text/event-stream
    return RestClient.create(restTemplate);
  }

//  @GetMapping
//  public void method(HttpServletResponse response) throws IOException {
//    response.getWriter().write("Data");
//  }

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
