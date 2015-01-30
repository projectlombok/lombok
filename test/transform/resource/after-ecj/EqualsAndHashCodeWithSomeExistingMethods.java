import lombok.*;
import static lombok.AccessLevel.NONE;
@Data @Getter(NONE) @Setter(NONE) class EqualsAndHashCodeWithSomeExistingMethods {
  int x;
  public int hashCode() {
    return 42;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("EqualsAndHashCodeWithSomeExistingMethods(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") EqualsAndHashCodeWithSomeExistingMethods() {
    super();
  }
}
@Data @Getter(NONE) @Setter(NONE) class EqualsAndHashCodeWithSomeExistingMethods2 {
  int x;
  protected boolean canEqual(Object other) {
    return false;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeWithSomeExistingMethods2)))
        return false;
    final EqualsAndHashCodeWithSomeExistingMethods2 other = (EqualsAndHashCodeWithSomeExistingMethods2) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("EqualsAndHashCodeWithSomeExistingMethods2(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") EqualsAndHashCodeWithSomeExistingMethods2() {
    super();
  }
}
@Data @Getter(NONE) @Setter(NONE) class EqualsAndHashCodeWithAllExistingMethods {
  int x;
  public int hashCode() {
    return 42;
  }
  public boolean equals(Object other) {
    return false;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("EqualsAndHashCodeWithAllExistingMethods(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") EqualsAndHashCodeWithAllExistingMethods() {
    super();
  }
}
@Data @Getter(AccessLevel.NONE) @Setter(lombok.AccessLevel.NONE) class EqualsAndHashCodeWithNoExistingMethods {
  int x;
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof EqualsAndHashCodeWithNoExistingMethods)))
        return false;
    final EqualsAndHashCodeWithNoExistingMethods other = (EqualsAndHashCodeWithNoExistingMethods) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.x != other.x))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof EqualsAndHashCodeWithNoExistingMethods);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.x);
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("EqualsAndHashCodeWithNoExistingMethods(x=" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") EqualsAndHashCodeWithNoExistingMethods() {
    super();
  }
}
