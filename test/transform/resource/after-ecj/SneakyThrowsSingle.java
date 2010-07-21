import java.io.IOException;
class SneakyThrowsSingle {
  SneakyThrowsSingle() {
    super();
  }
  public @lombok.SneakyThrows(Throwable.class) void test() {
    try 
      {
        System.out.println("test1");
      }
    catch (final Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @lombok.SneakyThrows(IOException.class) void test2() {
    try 
      {
        System.out.println("test2");
        throw new IOException();
      }
    catch (final IOException $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @lombok.SneakyThrows(value = IOException.class) void test3() {
    try 
      {
        System.out.println("test3");
        throw new IOException();
      }
    catch (final IOException $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
}
