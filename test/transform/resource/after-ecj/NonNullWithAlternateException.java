public class NonNullWithAlternateException {
  private @lombok.NonNull @lombok.Setter String test;
  public NonNullWithAlternateException() {
    super();
  }
  public void testMethod(@lombok.NonNull String arg) {
    if ((arg == null))
        {
          throw new java.lang.IllegalArgumentException("arg");
        }
    System.out.println(arg);
  }
  public @java.lang.SuppressWarnings("all") void setTest(final @lombok.NonNull String test) {
    if ((test == null))
        {
          throw new java.lang.IllegalArgumentException("test");
        }
    this.test = test;
  }
}