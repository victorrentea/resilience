package victor.training.resilience.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IdempotencyKey {
  @PostMapping("order")
  public void placeOrder() {

  }

  private final Set<String> lastSeenIdempotencyKeys = Collections.synchronizedSet(new HashSet<>());
  @PutMapping("order/{idempotencyKey}")
  public void placeOrder(String idempotencyKey) {
    if (!lastSeenIdempotencyKeys.add(idempotencyKey)) {
      throw new RuntimeException("Duplocated order request with idempotencyKey " + idempotencyKey);
    }
//    myEntity.setId(idempotencyKey);
  }
}
