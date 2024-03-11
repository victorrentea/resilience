package victor.training.resilience;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;
import victor.training.resilience.blocking.RateLimiterDemo;
import victor.training.resilience.blocking.RetryDemo;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class RateLimiterTest {
  @Autowired
  RateLimiterDemo rateLimiterDemo;

  @Test
  void rate() throws ExecutionException, InterruptedException {
    assertThat(rateLimiterDemo.rateFP()).isEqualTo("rate-limited-call");
    assertThat(rateLimiterDemo.rateFP()).isEqualTo("rate-limited-call");
    assertThat(rateLimiterDemo.rateFP()).isEqualTo("rate-limited-call");
    assertThatThrownBy(()->rateLimiterDemo.rateFP());

  }
}
