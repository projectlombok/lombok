import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodVarargs.Extensions.class) class ExtensionMethodVarargs {
  static class Extensions {
    Extensions() {
      super();
    }
    public static String format(String string, Object... params) {
      return String.format(string, params);
    }
  }
  ExtensionMethodVarargs() {
    super();
  }
  public void test() {
    Long l1 = 1l;
    long l2 = 1l;
    Integer i1 = 1;
    int i2 = 1;
    ExtensionMethodVarargs.Extensions.format("%d %d %d %d", l1, l2, i1, i2);
    ExtensionMethodVarargs.Extensions.format("%d", l1);
    ExtensionMethodVarargs.Extensions.format("", new Integer[]{1, 2});
    ExtensionMethodVarargs.Extensions.format("", new Integer[]{1, 2}, new Integer[]{1, 2});
  }
}