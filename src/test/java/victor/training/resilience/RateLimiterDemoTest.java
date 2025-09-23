package victor.training.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EnableWireMock
@TestPropertySource(properties = {
    "target.url.base=http://localhost:${wiremock.server.port}"
})
public class RateLimiterDemoTest {
  @Autowired
  RateLimiterDemo rateLimited;

  @Test // TODO default rate limiter of 3 req/min
  void global() throws InterruptedException {
    assertThat(rateLimited.global()).isEqualTo("OK");
    assertThat(rateLimited.global()).isEqualTo("OK");
    assertThat(rateLimited.global()).isEqualTo("OK");
    assertThatThrownBy(()-> rateLimited.global()).describedAs("call blocked");

    Thread.sleep(1100); // but after a while
    assertThat(rateLimited.global()).isEqualTo("OK");
  }

  @Test // TODO for tenant ABC, rate limit to 2 req/min
  void perTenant() throws InterruptedException {
    assertThat(rateLimited.perTenant("ABC")).isEqualTo("OK");
    assertThat(rateLimited.perTenant("ABC")).isEqualTo("OK");
    assertThatThrownBy(()-> rateLimited.perTenant("ABC")).describedAs("call blocked");

    Thread.sleep(1100); // but after a while
    assertThat(rateLimited.perTenant("ABC")).describedAs("accepted").isEqualTo("OK");
  }
}
