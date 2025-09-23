package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.EnableWireMock;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EnableWireMock
@TestPropertySource(properties = {
    "target.url.base=http://localhost:${wiremock.server.port}"
})
public class RateLimiterDemoTest {
  @Autowired
  RateLimiterDemo rateLimiterDemo;

  @Test
  void rate() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThatThrownBy(()->rateLimiterDemo.rateLimiter(""));
  }

  @Test
  void ratePerTenant() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateLimiter("tenant2")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("tenant2")).isEqualTo("OK");
    assertThatThrownBy(()->rateLimiterDemo.rateLimiter("tenant2"));
  }

  @Test
  void ratePerTenant_worksLater() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateLimiter("tenant1")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("tenant1")).isEqualTo("OK");
    assertThatThrownBy(()->rateLimiterDemo.rateLimiter("tenant1"));
    Thread.sleep(1100);
    assertThat(rateLimiterDemo.rateLimiter("tenant1")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("tenant1")).isEqualTo("OK");
  }
}
