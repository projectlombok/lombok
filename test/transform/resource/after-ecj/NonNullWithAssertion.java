public class NonNullWithAssertion {
  private @lombok.NonNull @lombok.Setter String test;
  public NonNullWithAssertion() {
    super();
  }
  public void testMethod(@lombok.NonNull String arg) {
    assert (arg != null): "arg is marked non-null but is null";
    System.out.println(arg);
  }
  public void testMethodWithIf(@lombok.NonNull String arg) {
    if ((arg == null))
        throw new NullPointerException("Oops");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setTest(final @lombok.NonNull String test) {
    assert (test != null): "test is marked non-null but is null";
    this.test = test;
  }
}
