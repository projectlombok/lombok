import java.util.Arrays;
import java.util.List;
import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodChain.Extensions.class) class ExtensionMethodChain {
  static class Extensions {
    Extensions() {
      super();
    }
    public static Integer intValue(String s) {
      return Integer.valueOf(s);
    }
  }
  ExtensionMethodChain() {
    super();
  }
  public void test() {
    ExtensionMethodChain.Extensions.intValue("1").intValue();
  }
}