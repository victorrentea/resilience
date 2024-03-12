package victor.training.resilience;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

@Observed
@Slf4j
@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableCaching
public class ClientApp {
  public static void main(String[] args) {
    SpringApplication.run(ClientApp.class, args);
  }

  private final CacheManager cacheManager;

  @EventListener(ApplicationStartedEvent.class)
  @Async // don't slow down startup
  public void checkCacheAutoEviction() throws InterruptedException {
    Cache cache = cacheManager.getCache("previous-response");
    cache.put("k", "value");
    Assert.isTrue(cache.get("k", String.class).equals("value"), "Cache is not working");
    Thread.sleep(4000);
    Assert.isNull(cache.get("k", String.class), "Cache is not auto-evicting");
  }

}
