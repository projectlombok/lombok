import java.io.IOException;
import java.util.Arrays;
import lombok.val;
public class ValToNative {
  public ValToNative() {
    super();
  }
  private void test() throws IOException {
    final @val var intField = 1;
    for (final @val var s : Arrays.asList("1")) 
      {
        final @val var s2 = s;
      }
    try (final @val var in = getClass().getResourceAsStream("ValToNative.class"))
      {
        final @val var j = in.read();
      }
  }
}