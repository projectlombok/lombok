import lombok.experimental.ExtensionMethod;
@ExtensionMethod(ExtensionMethodCacheTypeChange.Extensions.class) class ExtensionMethodCacheTypeChange {
  static class Base {
    Base() {
      super();
    }
    Number get() {
      return 1;
    }
    String text() {
      return "";
    }
    String count() {
      return "";
    }
  }
  static class Extensions {
    Extensions() {
      super();
    }
    public static String get(Base base) {
      return "";
    }
    public static String text(Base base) {
      return base.toString();
    }
    public static Integer count(Base base) {
      return 0;
    }
    public static String shout(String s) {
      return s;
    }
    public static Integer half(Integer i) {
      return i;
    }
    public static String pad(String s) {
      return s;
    }
    public static Integer pad(Integer i) {
      return i;
    }
  }
  ExtensionMethodCacheTypeChange() {
    super();
  }
  public void test() {
    Base base = new Base();
    ExtensionMethodCacheTypeChange.Extensions.shout(ExtensionMethodCacheTypeChange.Extensions.text(base));
    ExtensionMethodCacheTypeChange.Extensions.shout(ExtensionMethodCacheTypeChange.Extensions.get(base));
    ExtensionMethodCacheTypeChange.Extensions.shout(ExtensionMethodCacheTypeChange.Extensions.text(base));
    ExtensionMethodCacheTypeChange.Extensions.half(ExtensionMethodCacheTypeChange.Extensions.count(base));
    ExtensionMethodCacheTypeChange.Extensions.pad(ExtensionMethodCacheTypeChange.Extensions.count(base));
    ExtensionMethodCacheTypeChange.Extensions.pad(ExtensionMethodCacheTypeChange.Extensions.get(base));
    ExtensionMethodCacheTypeChange.Extensions.shout(ExtensionMethodCacheTypeChange.Extensions.text(base));
  }
}
