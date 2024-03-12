package victor.training.resilience;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Idempotency {
  @PostMapping("order")
  public void placeOrder(@RequestBody String order) {

  }

  private final Set<String> lastSeenIdempotencyKeys = Collections.synchronizedSet(new HashSet<>());
  @PutMapping("order/{idempotencyKey}")
  public void placeOrder(String idempotencyKey, @RequestBody String order) {
    if (!lastSeenIdempotencyKeys.add(idempotencyKey)) {
      throw new RuntimeException("Duplocated order request with idempotencyKey " + idempotencyKey);
    }
//    myEntity.setId(idempotencyKey);
  }
}
