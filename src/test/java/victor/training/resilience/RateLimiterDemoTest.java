package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
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

  @Test // TODO default rate limiter of 3 req/min
  void rate() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
    assertThatThrownBy(()->rateLimiterDemo.rateLimiter(""));
    Thread.sleep(1100);
    assertThat(rateLimiterDemo.rateLimiter("")).isEqualTo("OK");
  }

  @Test // TODO for tenant ABC, rate limit to 2 req/min
  void ratePerTenant_worksLater() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateLimiter("ABC")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("ABC")).isEqualTo("OK");
    assertThatThrownBy(()->rateLimiterDemo.rateLimiter("ABC"));
    Thread.sleep(1100);
    assertThat(rateLimiterDemo.rateLimiter("ABC")).isEqualTo("OK");
    assertThat(rateLimiterDemo.rateLimiter("ABC")).isEqualTo("OK");
  }
}
