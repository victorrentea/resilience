package victor.training.resilience;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
public class Idempotency {
  private final OrderRepo orderRepo;

  @GetMapping("orders")
  public Map<UUID, Order> getOrders() {
    return orderRepo.findAll();
  }

  // #1 Idempotency key window
  //  TODO should not be local to one instance => Redis/DB?
  //  TODO evict keys older than X seconds
  private final Set<String> recentIdempotencyKeys = Collections.synchronizedSet(new HashSet<>());
  @PostMapping("orders")
  public void createOrderIdempotencyHeader(
      @RequestHeader("X-Idempotency-Key") String idempotencyKey,
      @RequestBody String order) {
    if (!recentIdempotencyKeys.add(idempotencyKey)) {
      throw new RuntimeException("Duplicated request with same idempotencyKey " + idempotencyKey);
    }
    orderRepo.save(new Order(UUID.randomUUID(), order));
  }

  // #2 Client-generated PK
  @PutMapping("orders/{id}")
  public void createOrderWithClientPK(@PathVariable String id, @RequestBody String order) {
    orderRepo.save(new Order(UUID.randomUUID(), order));
  }

  // #3 "Common-Sense" window of recent requests (hashed)
  private final Set<String> recentContentsHashes = Collections.synchronizedSet(new HashSet<>());

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
