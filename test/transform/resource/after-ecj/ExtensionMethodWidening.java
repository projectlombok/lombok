import lombok.experimental.ExtensionMethod;
@ExtensionMethod({ExtensionMethodWidening.Extensions.class}) class ExtensionMethodWidening {
  static class Extensions {
    Extensions() {
      super();
    }
    public static String widening(String string, long a) {
      return ((string + " ") + a);
    }
  }
  ExtensionMethodWidening() {
    super();
  }
  public void test() {
    String string = "test";
    ExtensionMethodWidening.Extensions.widening(string, 1);
  }
}
