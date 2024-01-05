import lombok.SneakyThrows;
public class SneakyThrowsInInitializer {
  public static final Runnable R = new Runnable() {
    public @Override @SneakyThrows void run() {
      try
        {
          System.out.println("test");
        }
      catch (final java.lang.Throwable $ex)
        {
          throw lombok.Lombok.sneakyThrow($ex);
        }
    }
  };
  <clinit>() {
  }
  public SneakyThrowsInInitializer() {
  }
}
