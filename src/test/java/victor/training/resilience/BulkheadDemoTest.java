package victor.training.resilience;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class BulkheadDemoTest {
  @Autowired
  BulkheadDemo bulkheadDemo;

  @Test
  void explore() throws ExecutionException, InterruptedException {
    var f1 = supplyAsync(() -> bulkheadDemo.bulkheadFP());
    Thread.sleep(10);
    var f2 = supplyAsync(() -> bulkheadDemo.bulkheadFP());
    Thread.sleep(10);
    var f3 = supplyAsync(() -> bulkheadDemo.bulkheadFP());

    // Task 1 + 2 = running ✅, Task 3 = rejected ❌
    System.out.println("Patience: This tests should take several seconds to complete ...");
    assertThatThrownBy(f3::get) // 3rd ❌
        .describedAs("Third parallel call should've been rejected")
        .hasMessageContaining("Bulkhead");
    log.info("Third failed immediately");
    f1.get(); // 1st call ✅
    f2.get(); // 2nd call ✅
  }

  @Test
  void perTenant() throws ExecutionException, InterruptedException {
    var fa1 = supplyAsync(() -> bulkheadDemo.bulkheadPerTenant("a"));
    Thread.sleep(10);
    var fa2 = supplyAsync(() -> bulkheadDemo.bulkheadPerTenant("a"));
    Thread.sleep(10);
    var fa3 = supplyAsync(() -> bulkheadDemo.bulkheadPerTenant("a"));
    Thread.sleep(10);
    var fb = supplyAsync(() -> bulkheadDemo.bulkheadPerTenant("b"));

    System.out.println("Patience: This tests should take several seconds to complete ...");
    fa1.get();
    fa2.get();
    assertThatThrownBy(fa3::get).hasMessageContaining("Bulkhead");
    fb.get();
  }
}
