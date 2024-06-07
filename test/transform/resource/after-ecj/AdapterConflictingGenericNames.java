import lombok.experimental.Adapter;
import java.util.List;
public class AdapterConflictingGenericNames<T> {
  public static @Adapter(silent = true) class AdapterClass implements FooInterface {
    public AdapterClass() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated <T extends java.lang.Object>java.util.List<T> getListOfType(final java.lang.Class<T> clazz) {
      return java.util.Collections.emptyList();
    }
  }
  public static interface FooInterface {
    <T>List<T> getListOfType(Class<T> clazz);
  }
  public AdapterConflictingGenericNames() {
    super();
  }
}
