import lombok.experimental.Adapter;
public class AdapterDefaultSettings {
  public static @Adapter class AdapterClass implements FooInterface {
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
  }
  public static interface FooInterface {
    void doStuff();
    int countMyObjects(String userName) throws IllegalArgumentException;
    String getStatus() throws IllegalStateException;
  }
  public AdapterDefaultSettings() {
    super();
  }
}
