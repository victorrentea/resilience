package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.wiremock.spring.EnableWireMock;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableWireMock
@TestPropertySource(properties = "target.url.base=http://localhost:${wiremock.server.port}")
public class RetryDemoTest {
  public static final String ERROR_PAYLOAD = "MOCK-ERROR";
  public static final String OK_PAYLOAD = "MOCK-OK";
  @Autowired
  RetryDemo retryDemo;

  @Test
  void persistentFailure() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withStatus(500).withBody(ERROR_PAYLOAD))
    );

    Date t0 = new Date();
    assertThatThrownBy(() -> retryDemo.retryFP())
        .isInstanceOf(HttpServerErrorException.InternalServerError.class)
        .hasMessageContaining(ERROR_PAYLOAD);

    List<LoggedRequest> calls = findAll(anyRequestedFor(anyUrl()));
    assertThat(calls).hasSize(3);
    assertThat(calls.get(0).getLoggedDate()).isCloseTo(t0, 10);
    assertThat(calls.get(1).getLoggedDate()).isCloseTo(new Date(t0.getTime()+110), 50);
    assertThat(calls.get(2).getLoggedDate()).isCloseTo(new Date(t0.getTime()+210), 50);
  }

  @Test
  void oneFailure() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withStatus(500).withBody(ERROR_PAYLOAD))
        .willReturn(aResponse().withStatus(200).withBody(OK_PAYLOAD))
    );

    assertThat(retryDemo.retryFP()).isEqualTo(OK_PAYLOAD);
  }

  @Test
  void twoFailures() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withStatus(500).withBody(ERROR_PAYLOAD))
        .willReturn(aResponse().withStatus(500).withBody(ERROR_PAYLOAD))
        .willReturn(aResponse().withStatus(200).withBody(OK_PAYLOAD))
    );

    assertThat(retryDemo.retryFP()).isEqualTo(OK_PAYLOAD);
  }

  @Test
  void badRequestIsNotRetried() throws ExecutionException, InterruptedException {
    WireMock.stubFor(get(urlEqualTo("/retry-api"))
        .willReturn(aResponse().withBody(ERROR_PAYLOAD).withStatus(400)));

    assertThatThrownBy(() -> retryDemo.retryFP())
        .isInstanceOf(HttpClientErrorException.BadRequest.class)
        .hasMessageContaining(ERROR_PAYLOAD);

    WireMock.verify(1, getRequestedFor(urlEqualTo("/retry-api")));
  }
}
