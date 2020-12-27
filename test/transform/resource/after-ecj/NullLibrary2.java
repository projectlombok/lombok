public @lombok.EqualsAndHashCode @lombok.ToString @lombok.AllArgsConstructor class NullLibrary2 {
  @lombok.With String foo;
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") NullLibrary2 withFoo(final String foo) {
    return ((this.foo == foo) ? this : new NullLibrary2(foo));
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final @org.springframework.lang.Nullable java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof NullLibrary2)))
        return false;
    final NullLibrary2 other = (NullLibrary2) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    final java.lang.Object this$foo = this.foo;
    final java.lang.Object other$foo = other.foo;
    if (((this$foo == null) ? (other$foo != null) : (! this$foo.equals(other$foo))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final @org.springframework.lang.Nullable java.lang.Object other) {
    return (other instanceof NullLibrary2);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $foo = this.foo;
    result = ((result * PRIME) + (($foo == null) ? 43 : $foo.hashCode()));
    return result;
  }
  public @java.lang.Override @org.springframework.lang.NonNull @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("NullLibrary2(foo=" + this.foo) + ")");
  }
  public @java.lang.SuppressWarnings("all") NullLibrary2(final String foo) {
    super();
    this.foo = foo;
  }
}
