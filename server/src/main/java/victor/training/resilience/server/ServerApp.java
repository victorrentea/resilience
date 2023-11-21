package victor.training.resilience.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static java.time.Duration.ofSeconds;

@Slf4j
@RequiredArgsConstructor
@RestController
@SpringBootApplication
public class ServerApp {
  public static void main(String[] args) {
    SpringApplication.run(ServerApp.class, args);
  }

  @GetMapping("ok")
  public String ok() {
    log.info("CALl to OK");
    return "ok";
  }

  @GetMapping("timeout")
  public Mono<String> timeout() {
    if (Math.random() < .5) {
      return Mono.just("fast");
    } else {
      return Mono.just("slow")
          .delayElement(ofSeconds(3));
    }
  }

  @GetMapping("fail-half")
  public String failHalf() {
    if (Math.random() < .5) {
//      throw new IllegalArgumentException("FATAL ERROR: DON'T RETRY, eg invalid request parameters!");
      throw new RuntimeException("RETRYABLE ERROR, eg downstream system failed, optimistic locking error");
//      throw new RetryableException(); // explicit error response body
    } else {
      return "OK " + LocalDateTime.now();
    }
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String clientFault() {
    return "Client's fault! Don't retry";
  }

  static class RetryableException extends RuntimeException { }
  @ExceptionHandler(RetryableException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String retryableError() {
    return """
        {"retryable":true}
        """;
  }
}
