import lombok.experimental.ExtensionMethod;
@ExtensionMethod(Extensions.class) class ExtensionMethodSuppress {
  ExtensionMethodSuppress() {
    super();
  }
  public void test() {
    Test.staticMethod();
    Test test = new Test();
    Extensions.instanceMethod(test);
    Extensions.staticMethod(test);
  }
}
@ExtensionMethod(value = Extensions.class,suppressBaseMethods = false) class ExtensionMethodKeep {
  ExtensionMethodKeep() {
    super();
  }
  public void test() {
    Test.staticMethod();
    Test test = new Test();
    test.instanceMethod();
    test.staticMethod();
  }
}
class Test {
  Test() {
    super();
  }
  public static void staticMethod() {
  }
  public void instanceMethod() {
  }
}
class Extensions {
  Extensions() {
    super();
  }
  public static void staticMethod(Test test) {
  }
  public static void instanceMethod(Test test) {
  }
}