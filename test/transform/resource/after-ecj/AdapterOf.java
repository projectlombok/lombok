import java.util.Optional;
import lombok.experimental.Adapter;
public class AdapterOf {
  public static abstract @Adapter(of = {FooInterface.class, GooInterface.class}) class AdapterClass implements FooInterface, GooInterface, HooInterface {
    public AdapterClass() {
      super();
    }
    public @Override String getStatus() {
      return "TestStatus";
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated int countMyObjects(final java.lang.String userName) throws java.lang.IllegalArgumentException {
      throw new java.lang.UnsupportedOperationException();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated void doStuff() {
      throw new java.lang.UnsupportedOperationException();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated java.util.Optional<java.lang.String> gooMethod() {
      throw new java.lang.UnsupportedOperationException();
    }
  }
  public static interface FooInterface {
    void doStuff();
    int countMyObjects(String userName) throws IllegalArgumentException;
    String getStatus() throws IllegalStateException;
  }
  public static interface GooInterface {
    Optional<String> gooMethod();
  }
  public static interface HooInterface {
    void wontImplement();
  }
  public AdapterOf() {
    super();
  }
}
