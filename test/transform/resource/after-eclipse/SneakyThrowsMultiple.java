import java.awt.AWTException;
import java.io.IOException;
import java.util.Random;
class SneakyThrowsMultiple {
  SneakyThrowsMultiple() {
    super();
  }
  public @lombok.SneakyThrows({IOException.class, Throwable.class}) void test() {
    try 
      {
        try 
          {
            System.out.println("test1");
            throw new IOException();
          }
        catch (final IOException $ex)           {
            throw lombok.Lombok.sneakyThrow($ex);
          }
      }
    catch (final Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @lombok.SneakyThrows({AWTException.class, IOException.class}) void test2() {
    try 
      {
        try 
          {
            System.out.println("test2");
            if (new Random().nextBoolean())
                {
                  throw new IOException();
                }
            else
                {
                  throw new AWTException("WHAT");
                }
          }
        catch (final AWTException $ex)           {
            throw lombok.Lombok.sneakyThrow($ex);
          }
      }
    catch (final IOException $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
  public @lombok.SneakyThrows(value = {IOException.class, Throwable.class}) void test3() {
    try 
      {
        try 
          {
            System.out.println("test3");
            throw new IOException();
          }
        catch (final IOException $ex)           {
            throw lombok.Lombok.sneakyThrow($ex);
          }
      }
    catch (final Throwable $ex)       {
        throw lombok.Lombok.sneakyThrow($ex);
      }
  }
}