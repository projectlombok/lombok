import lombok.experimental.ExtensionMethod;
@ExtensionMethod({ExtensionMethodAutoboxing.Extensions.class}) class ExtensionMethodAutoboxing {
  static class Extensions {
    Extensions() {
      super();
    }
    public static String boxing(String string, Long a, int b) {
      return ((((string + " ") + a) + " ") + b);
    }
  }
  ExtensionMethodAutoboxing() {
    super();
  }
  public void test() {
    Long l1 = 1l;
    long l2 = 1l;
    Integer i1 = 1;
    int i2 = 1;
    String string = "test";
    ExtensionMethodAutoboxing.Extensions.boxing(string, l1, i1);
    ExtensionMethodAutoboxing.Extensions.boxing(string, l1, i2);
    ExtensionMethodAutoboxing.Extensions.boxing(string, l2, i1);
    ExtensionMethodAutoboxing.Extensions.boxing(string, l2, i2);
  }
}