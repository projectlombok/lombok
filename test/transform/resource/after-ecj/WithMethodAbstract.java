abstract class WithMethodAbstract {
  @lombok.With String foo;
  WithMethodAbstract() {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public abstract @java.lang.SuppressWarnings("all") WithMethodAbstract withFoo(final String foo);
}
