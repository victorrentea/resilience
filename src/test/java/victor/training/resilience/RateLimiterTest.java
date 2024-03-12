package victor.training.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

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
