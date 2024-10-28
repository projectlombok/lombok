public @lombok.EqualsAndHashCode @lombok.ToString @lombok.AllArgsConstructor class NullLibrary1 {
  @lombok.With String foo;
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @org.eclipse.jdt.annotation.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated NullLibrary1 withFoo(final String foo) {
    return ((this.foo == foo) ? this : new NullLibrary1(foo));
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.@org.eclipse.jdt.annotation.Nullable Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof NullLibrary1)))
        return false;
    final NullLibrary1 other = (NullLibrary1) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    final java.lang.Object this$foo = this.foo;
    final java.lang.Object other$foo = other.foo;
    if (((this$foo == null) ? (other$foo != null) : (! this$foo.equals(other$foo))))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.@org.eclipse.jdt.annotation.Nullable Object other) {
    return (other instanceof NullLibrary1);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $foo = this.foo;
    result = ((result * PRIME) + (($foo == null) ? 43 : $foo.hashCode()));
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.@org.eclipse.jdt.annotation.NonNull String toString() {
    return (("NullLibrary1(foo=" + this.foo) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated NullLibrary1(final String foo) {
    super();
    this.foo = foo;
  }
}
