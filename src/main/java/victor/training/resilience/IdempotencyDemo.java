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

  @GetMapping("orders")
  public Map<UUID, Order> getOrders() {
    return orderRepo.findAll();
  }

  // #1 Idempotency key window
  //  TODO state should not be local to one instance => move to Redis/DB?
  //  TODO evict keys older than X seconds on a timer?
  private final Set<String> recentIdempotencyKeys = Collections.synchronizedSet(new HashSet<>());
  @PostMapping("orders/ik")
  public void withIdempotencyHeader(
      @RequestHeader("X-Idempotency-Key") String idempotencyKey,
      @RequestBody String order) {
    boolean ikAlreadyReceived = recentIdempotencyKeys.add(idempotencyKey);
    if (!ikAlreadyReceived) {
      throw new RuntimeException("Duplicated request with same idempotencyKey " + idempotencyKey);
    }
    orderRepo.save(new Order(UUID.randomUUID(), order));
  }

  // #2 Client-generated PK
  @PutMapping("orders/{uuid}")
  public void withClientPK(@PathVariable UUID uuid, @RequestBody String order) {
    orderRepo.save(new Order(uuid, order));
  }

  // #3 "Common-Sense" window of recent requests (hashed)
  private final Set<HashCode> recentContentsHashes = Collections.synchronizedSet(new HashSet<>());

  @PostMapping("orders")
  public void withContentHashing(@RequestBody String order) {
    HashCode contentHash = Hashing.sha512().hashBytes(order.getBytes());
    boolean hashAlreadyReceived = recentContentsHashes.add(contentHash);
    if (!hashAlreadyReceived) {
      throw new RuntimeException("Duplicated request with same content");
    }
    orderRepo.save(new Order(UUID.randomUUID(), order));
  }

  // --- support code
  public record Order(UUID id, String contents){}

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
