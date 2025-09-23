package victor.training.resilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
public class IdempotencyDemoTest {
  @Autowired
  IdempotencyDemo idempotencyDemo;

  @Test
  void byIdempotencyKey() {
    String ik = UUID.randomUUID().toString();
    idempotencyDemo.byIdempotencyHeader(ik, "ik");
    try {
      idempotencyDemo.byIdempotencyHeader(ik, "ik"); // retry
    } catch (Exception ignored) {
    }

    assertThat(idempotencyDemo.getOrders().values())
        .map(IdempotencyDemo.Order::contents)
        .containsOnlyOnceElementsOf(List.of("ik"));
  }

  @Test
  void byClientPK() {
    UUID orderId = UUID.randomUUID();
    idempotencyDemo.byClientPK(orderId, "client-id");
    try {
      idempotencyDemo.byClientPK(orderId, "client-id"); // retry
    } catch (Exception ignored) {
    }

    assertThat(idempotencyDemo.getOrders())
        .extractingFromEntries(Map.Entry::getKey, e -> e.getValue().contents())
        .containsOnlyOnce(tuple(orderId, "client-id"));
  }

  @Test
  void byContentHashing() {
    idempotencyDemo.byContentHashing("content-hash");
    try {
      idempotencyDemo.byContentHashing("content-hash"); // retry
    } catch (Exception ignored) {
    }

    assertThat(idempotencyDemo.getOrders().values())
        .map(IdempotencyDemo.Order::contents)
        .containsOnlyOnce("content-hash");
  }
}
