public class NonNullWithAlternateException {
  private @lombok.NonNull @lombok.Setter String test;
  public NonNullWithAlternateException() {
    super();
  }
  public void testMethod(@lombok.NonNull String arg) {
    if ((arg == null))
        {
          throw new java.lang.IllegalArgumentException("arg is null");
        }
    System.out.println(arg);
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setTest(final @lombok.NonNull String test) {
    if ((test == null))
        {
          throw new java.lang.IllegalArgumentException("test is null");
        }
    this.test = test;
  }
}