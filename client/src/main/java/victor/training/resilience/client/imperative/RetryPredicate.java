package victor.training.resilience.client.imperative;

import java.util.function.Predicate;

public class RetryPredicate implements Predicate<Throwable> {
  @Override
  public boolean test(Throwable throwable) {
    return false;
  }
}
