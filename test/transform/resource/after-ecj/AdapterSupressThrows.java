import lombok.experimental.Adapter;
public class AdapterSupressThrows {
  public static @Adapter(suppressThrows = true) class AdapterClass implements FooInterface {
    public AdapterClass() {
      super();
    }
    public @Override String getStatus() {
      return "TestStatus";
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated int countMyObjects(final java.lang.String userName) {
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
  public AdapterSupressThrows() {
    super();
  }
}
