import lombok.SneakyThrows;
class SneakyThrowsPlain {
  SneakyThrowsPlain() {
    super();
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
