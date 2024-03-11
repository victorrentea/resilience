package victor.training.resilience;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import victor.training.resilience.blocking.BulkheadDemo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@SpringBootTest
public class BulkheadTest {
  @Autowired
  BulkheadDemo bulkheadDemo;

  @Test
  void explore() throws ExecutionException, InterruptedException {
    var f1 = supplyAsync(() -> bulkheadDemo.bulkhead());
    Thread.sleep(10);
    var f2 = supplyAsync(() -> bulkheadDemo.bulkhead());
    Thread.sleep(10);
    var f3 = supplyAsync(() -> bulkheadDemo.bulkhead());
    System.out.println("Patience: This tests should take several seconds to complete ...");
    f1.get();
    f2.get();
    Assertions.assertThatThrownBy(f3::get).hasMessageContaining("Bulkhead");
  }
}
