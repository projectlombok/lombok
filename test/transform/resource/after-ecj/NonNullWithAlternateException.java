public class NonNullWithAlternateException {
  private @lombok.NonNull @lombok.Setter String test;
  public NonNullWithAlternateException() {
    super();
  }
  public void testMethod(@lombok.NonNull String arg) {
    if ((arg == null))
        {
          throw new java.lang.IllegalArgumentException("arg is marked non-null but is null");
        }
    System.out.println(arg);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setTest(final @lombok.NonNull String test) {
    if ((test == null))
        {
          throw new java.lang.IllegalArgumentException("test is marked non-null but is null");
        }
    this.test = test;
  }
}
