package victor.training.resilience;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
public class IdempotencyDemo {
  private final OrderRepo orderRepo;

  // ==== Option 1: Idempotency key window ====
  private final Set<String> recentIdempotencyKeys = Collections.synchronizedSet(new HashSet<>());
  @PostMapping("orders/ik")
  public void byIdempotencyHeader(
      @RequestHeader("X-Idempotency-Key") String idempotencyKey,
      @RequestBody String order) {
    if (recentIdempotencyKeys.contains(idempotencyKey)) return; // duplicate!
    orderRepo.save(new Order(UUID.randomUUID(), order));
    recentIdempotencyKeys.add(idempotencyKey);
  }

  // TODO risk of storing this in-memory? ....
  // TODO [optional] evict entries older than X seconds after 3 seconds

  // ==== Option 2: Client-generated PK ====
  @PutMapping("orders/{uuid}")
  public void byClientPK(@PathVariable UUID uuid, @RequestBody String order) {
    orderRepo.save(new Order(uuid,order)); // PK violation on dups
  }

  // ==== Option 3: window of recent requests payloads (hashed) ====
  private final Set<HashCode> recentContentsHashes = Collections.synchronizedSet(new HashSet<>());
  @PostMapping("orders")
  public void byContentHashing(@RequestBody String order) {
    HashCode orderHash = Hashing.sha256().hashUnencodedChars(order);
    if (recentContentsHashes.contains(orderHash)) {
      return; // duplicate!
    }
    orderRepo.save(new Order(UUID.randomUUID(), order));
  }

  // =============== support code =================

  @GetMapping("orders")
  public Map<UUID, Order> getOrders() {
    return orderRepo.findAll();
  }

  public record Order(UUID id, String contents) {}

  @Repository
  static class OrderRepo{
    private final Map<UUID, Order> data = new HashMap<>();

    public void save(Order order) {
      if (data.putIfAbsent(order.id(), order) != null) {
        throw new IllegalArgumentException("PK Violation");
      }
    }

    public Map<UUID, Order> findAll() {
      return Collections.unmodifiableMap(data);
    }
  }
}

