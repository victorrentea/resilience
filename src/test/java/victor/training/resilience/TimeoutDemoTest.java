package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.wiremock.spring.EnableWireMock;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@EnableWireMock
@TestPropertySource(properties = "target.url.base=http://localhost:${wiremock.server.port}")
public class TimeoutDemoTest {
  @Autowired
  TimeoutDemo timeoutDemo;

  @Test
  void timeoutAfter2Sec() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/timeout-api"))
        .willReturn(aResponse().withStatus(200).withBody("OK-BUT-TOO-LATE")
            .withFixedDelay(3000))
    );

    assertThatThrownBy(() -> timeoutDemo.timeout())
        .isInstanceOf(ResourceAccessException.class)
        .hasMessageContaining("Read timed out");
  }
}
