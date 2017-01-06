@lombok.Data(staticConstructor = "of") @lombok.NoArgsConstructor class ConflictingStaticConstructorNames {
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof ConflictingStaticConstructorNames)))
        return false;
    final ConflictingStaticConstructorNames other = (ConflictingStaticConstructorNames) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof ConflictingStaticConstructorNames);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int hashCode() {
    int result = 1;
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
    return "ConflictingStaticConstructorNames()";
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated ConflictingStaticConstructorNames() {
    super();
  }
}