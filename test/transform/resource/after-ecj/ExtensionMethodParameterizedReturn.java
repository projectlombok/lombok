import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodParameterizedReturn.Extensions.class) class ExtensionMethodParameterizedReturn {
  static class Extensions {
    Extensions() {
      super();
    }
    public static <V, R>List<R> mapAllToList(Collection<? extends V> values, Function<V, R> mapper) {
      return null;
    }
  }
  ExtensionMethodParameterizedReturn() {
    super();
  }
  void test() {
    List<String> values = null;
    process(ExtensionMethodParameterizedReturn.Extensions.mapAllToList(values, (<no type> s) -> (s + ";")));
  }
  void process(List<String> values) {
  }
}
