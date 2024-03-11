import base.GatlingEngine;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static java.time.Duration.ofSeconds;

public class CircuitLoadTest extends Simulation {
  public static void main(String[] args) {
    GatlingEngine.startClass(CircuitLoadTest.class);
  }

  {
    setUp(scenario(getClass().getSimpleName())
        .exec(http("").get("/circuit"))
        // 10 requests per second for 30 seconds
        .injectOpen(constantUsersPerSec(10d).during(ofSeconds(30)))
        .protocols(http.baseUrl("http://localhost:8080")));
  }
}
