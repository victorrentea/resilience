package victor.training.resilience;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import victor.training.resilience.blocking.RetryDemo;
import victor.training.resilience.blocking.TimeoutDemo;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "target.url.base=http://localhost:${wiremock.server.port}")
public class RetryTest {
  @Autowired
  RetryDemo retryDemo;

  @Test
  void explore() throws ExecutionException, InterruptedException {
    stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withBody("MOCK-ERROR").withStatus(500)));
    retryDemo.retryFP();
  }
}
