package victor.training.resilience.client;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.function.Predicate;

public class RetryPredicate implements Predicate<Throwable> {
  @Override
  public boolean test(Throwable throwable) {
    if (!(throwable instanceof WebClientResponseException.InternalServerError e)) {
      return false;
    }
    record ErrorResponseBody(boolean retryable) {
    }
    ErrorResponseBody responseBody = e.getResponseBodyAs(ErrorResponseBody.class);
    return responseBody.retryable;
  }
}
