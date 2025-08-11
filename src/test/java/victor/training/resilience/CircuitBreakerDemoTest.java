package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;
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
public class CircuitBreakerDemoTest {
  @Autowired
  CircuitBreakerDemo circuitBreakerDemo;

  @Test
  void circuit() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/circuit-api"))
        .willReturn(aResponse().withStatus(503).withBody("BUSY").withFixedDelay(10))
    );

    assertThatThrownBy(() -> {
      for (int i = 0; i < 200; i++) {
        try {
          circuitBreakerDemo.circuitFP();
        } catch (HttpServerErrorException.ServiceUnavailable error) {
          System.out.println("503");
        }
      }
    }).isInstanceOf(CallNotPermittedException.class);
    System.out.println("NOW OPEN. Sleeping 2s");

    Thread.sleep(2100);

    System.out.println("NOW IN HALF_OPEN");
    WireMock.stubFor(get(urlEqualTo("/circuit-api"))
        .willReturn(aResponse().withStatus(200).withBody("OK").withFixedDelay(10))
    );
    for (int i = 0; i < 200; i++) {
      circuitBreakerDemo.circuitFP();
    }
    System.out.println("NOW CLOSED");
  }
}
