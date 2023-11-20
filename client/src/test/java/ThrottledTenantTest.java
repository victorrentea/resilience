import base.GatlingEngine;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static java.time.Duration.ofSeconds;

public class ThrottledTenantTest extends Simulation {
  public static void main(String[] args) {
    GatlingEngine.startClass(ThrottledTenantTest.class);
  }

  {
    setUp(scenario(getClass().getSimpleName())
        .exec(http("").get("/throttled-tenant"))
        .injectClosed(constantConcurrentUsers(5).during(ofSeconds(5))))
        .protocols(http.baseUrl("http://localhost:8080"))
        .assertions(global().successfulRequests().percent().gt(99.0));
  }
}
