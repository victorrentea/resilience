package victor.training.resilience;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
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

  @Test // when the error does not go away, 3 attempts are made with a fixed backoff of 100ms
  void persistentFailure() throws ExecutionException, InterruptedException {
    wireMockSequence(get(urlEqualTo("/retry-api")),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD)
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

  @Test // the second attempt succeeds
  void oneFailure() throws ExecutionException, InterruptedException {
    wireMockSequence(get(urlEqualTo("/retry-api")),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD),
        aResponse().withStatus(200).withBody(OK_PAYLOAD)
    );

    assertThat(retryDemo.retryFP()).isEqualTo(OK_PAYLOAD);
  }

  @Test // the third attempt succeeds
  void twoFailures() throws ExecutionException, InterruptedException {
    wireMockSequence(get(urlEqualTo("/retry-api")),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD),
        aResponse().withStatus(500).withBody(ERROR_PAYLOAD),
        aResponse().withStatus(200).withBody(OK_PAYLOAD)
    );

    assertThat(retryDemo.retryFP()).isEqualTo(OK_PAYLOAD);
  }

  @Test // a 400 Bad Request response is not retried
  // thrown by RestClient as HttpClientErrorException.BadRequest
  void badRequestIsNotRetried() throws ExecutionException, InterruptedException {
    wireMockSequence(get(urlEqualTo("/retry-api")),
        aResponse().withBody(ERROR_PAYLOAD).withStatus(400));

    assertThatThrownBy(() -> retryDemo.retryFP())
        .isInstanceOf(HttpClientErrorException.BadRequest.class)
        .hasMessageContaining(ERROR_PAYLOAD);

    WireMock.verify(1, getRequestedFor(urlEqualTo("/retry-api")));
  }

  public static void wireMockSequence(MappingBuilder request, ResponseDefinitionBuilder... responses) {
    String scenarioName = UUID.randomUUID().toString();
    stubFor(request
        .inScenario(scenarioName)
        .whenScenarioStateIs(STARTED)
        .willReturn(responses[0])
        .willSetStateTo("STEP1"));
    for (int i = 1; i < responses.length; i++) {
      stubFor(request
          .withId(UUID.randomUUID())
          .inScenario(scenarioName)
          .whenScenarioStateIs("STEP"+i)
          .willReturn(responses[i])
          .willSetStateTo("STEP"+(i+1)));

    }
  }

}
