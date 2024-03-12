package victor.training.resilience;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "target.url.base=http://localhost:${wiremock.server.port}")
public class RetryTest {
  @Autowired
  RetryDemo retryDemo;
  @BeforeEach
  final void before() {
      resetAllRequests();
  }

  @Test
  void explore500() throws ExecutionException, InterruptedException {
    stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withBody("MOCK-ERROR").withStatus(500)));

    Assertions.assertThatThrownBy(() -> retryDemo.retryFP())
        .isInstanceOf(HttpServerErrorException.InternalServerError.class)
        .hasMessageContaining("MOCK-ERROR");

    verify(3, getRequestedFor(urlEqualTo("/retry-api")));
  }
  @Test
  void explore400() throws ExecutionException, InterruptedException {
    stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withBody("MOCK-ERROR").withStatus(400)));

    Assertions.assertThatThrownBy(() -> retryDemo.retryFP())
        .hasMessageContaining("MOCK-ERROR");

    verify(1, getRequestedFor(urlEqualTo("/retry-api")));
  }
}
