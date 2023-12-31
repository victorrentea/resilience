import base.GatlingEngine;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static java.time.Duration.ofSeconds;

public class RateLimiterTest extends Simulation {
  public static void main(String[] args) {
    GatlingEngine.startClass(RateLimiterTest.class);
  }

  {
    String host = "http://localhost:8080";

    setUp(scenario(getClass().getSimpleName())
        .exec(http("").get("/rate"))
        .injectClosed(constantConcurrentUsers(5).during(ofSeconds(5))))
        .protocols(http.baseUrl(host))
        .assertions(global().successfulRequests().percent().gt(99.0));
  }
}
