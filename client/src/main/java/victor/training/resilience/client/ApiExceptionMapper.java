package victor.training.resilience.client;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionMapper {
  @ExceptionHandler(BulkheadFullException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  // @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) // if limiting per-client
  public String onBulkhead() {
    return "Please try again later";
  }
}
