import lombok.val;
import java.io.IOException;
public class ValInTryWithResources {
  public ValInTryWithResources() {
    super();
  }
  public void whyTryInsteadOfCleanup() throws IOException {
    try (final @val java.io.InputStream in = getClass().getResourceAsStream("ValInTryWithResources.class"))
      {
        final @val java.io.InputStream i = in;
        final @val int j = in.read();
      }
  }
}
