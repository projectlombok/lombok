import javax.annotation.Nonnull;
class NonNullJavaxOnParameter extends Thread {
  <clinit>() {
  }
  NonNullJavaxOnParameter(@javax.annotation.Nonnull String arg) {
    this(arg, "");
    if ((arg == null))
        {
          throw new java.lang.NullPointerException("arg is marked non-null but is null");
        }
  }
  NonNullJavaxOnParameter(@javax.annotation.Nonnull String arg, @javax.annotation.Nonnull String arg2) {
    super(arg);
    if ((arg2 == null))
        {
          throw new java.lang.NullPointerException("arg2 is marked non-null but is null");
        }
    if ((arg == null))
        throw new NullPointerException();
  }
  public void test2(@javax.annotation.Nonnull String arg, @javax.annotation.Nonnull String arg2, @javax.annotation.Nonnull String arg3) {
    if ((arg == null))
        {
          throw new java.lang.NullPointerException("arg is marked non-null but is null");
        }
    if ((arg3 == null))
        {
          throw new java.lang.NullPointerException("arg3 is marked non-null but is null");
        }
    if ((arg2 == null))
        {
          throw new NullPointerException("arg2");
        }
    if ((arg == null))
        System.out.println("Hello");
  }
  public void test3(@javax.annotation.Nonnull String arg) {
    if ((arg == null))
        {
          throw new java.lang.NullPointerException("arg is marked non-null but is null");
        }
    if ((arg != null))
        throw new IllegalStateException();
  }
  public void test(@javax.annotation.Nonnull String stringArg, @Nonnull String arg2, @javax.annotation.Nonnull int primitiveArg) {
    if ((stringArg == null))
        {
          throw new java.lang.NullPointerException("stringArg is marked non-null but is null");
        }
    if ((arg2 == null))
        {
          throw new java.lang.NullPointerException("arg2 is marked non-null but is null");
        }
  }
  public void test(@javax.annotation.Nonnull String arg) {
    if ((arg == null))
        {
          throw new java.lang.NullPointerException("arg is marked non-null but is null");
        }
    System.out.println("Hey");
    if ((arg == null))
        throw new NullPointerException();
  }
  public void testWithAssert(@javax.annotation.Nonnull String param) {
    assert (param != null);
  }
  public void testWithAssertAndMessage(@javax.annotation.Nonnull String param) {
    assert (param != null): "Oops";
  }
}
