package a;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(Extensions.class) class ExtensionMethodNames {
  ExtensionMethodNames() {
    super();
  }
  public void instanceCalls() {
    a.Extensions.ext(new Test());
    Test t = new Test();
    a.Extensions.ext(t);
    Test Test = new Test();
    a.Extensions.ext(Test);
  }
  public void staticCalls() {
    Test.ext();
    a.Test.ext();
  }
}
class Extensions {
  Extensions() {
    super();
  }
  public static void ext(Test t) {
  }
}
class Test {
  Test() {
    super();
  }
  public static void ext() {
  }
}