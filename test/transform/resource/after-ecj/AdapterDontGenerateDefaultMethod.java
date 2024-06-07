import lombok.experimental.Adapter;
public class AdapterDontGenerateDefaultMethod {
  public static @Adapter class AdapterClass implements FooInterface {
    public AdapterClass() {
      super();
    }
    public @Override String getStatus() {
      return "TestStatus";
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated void doStuff() {
      throw new java.lang.UnsupportedOperationException();
    }
  }
  public static interface FooInterface {
    void doStuff();
    default int countMyObjects(String userName) throws IllegalArgumentException {
      return 1;
    }
    String getStatus() throws IllegalStateException;
  }
  public AdapterDontGenerateDefaultMethod() {
    super();
  }
}
