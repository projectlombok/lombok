import lombok.SneakyThrows;
class SneakyThrowsPlain {
  @lombok.SneakyThrows SneakyThrowsPlain() {
    super();
    try 
      {
        System.out.println("constructor");
      }
    catch (final java.lang.Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  @lombok.SneakyThrows SneakyThrowsPlain(int x) {
    this();
    try 
      {
        System.out.println("constructor2");
      }
    catch (final java.lang.Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @lombok.SneakyThrows void test() {
    try 
      {
        System.out.println("test1");
      }
    catch (final java.lang.Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @SneakyThrows void test2() {
    try 
      {
        System.out.println("test2");
      }
    catch (final java.lang.Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
}
