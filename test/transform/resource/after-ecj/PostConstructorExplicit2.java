import lombok.experimental.PostConstructor;
import lombok.experimental.PostConstructor.InvokePostConstructors;
class PostConstructorExplicit2 {
  private @PostConstructor void first() {
  }
  private @PostConstructor void second() {
  }
  public @InvokePostConstructors PostConstructorExplicit2(long tryFinally) throws java.io.IOException {
    super();
    boolean $callPostConstructor = true;
    try
      {
        if ("a".equals("b"))
            {
              return ;
            }
        if ("a".equals("b"))
            {
              throw new java.io.IOException("");
            }
      }
    catch (final java.lang.Throwable $ex)
      {
        $callPostConstructor = false;
        throw $ex;
      }
    finally
      {
        if ($callPostConstructor)
            {
              first();
              second();
            }
      }
  }
  public @InvokePostConstructors PostConstructorExplicit2(int innerExit) {
    super();
    Runnable r1 = new Runnable() {
      x() {
        super();
      }
      public void run() {
        return ;
      }
    };
    first();
    second();
  }
  public @InvokePostConstructors PostConstructorExplicit2(short lambdaExit) {
    super();
    Runnable r2 = () ->     {
      return ;
    };
    first();
    second();
  }
}
