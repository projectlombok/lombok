@lombok.EqualsAndHashCode class EqualsAndHashCodeWithExistingMethods {
  int x;
  EqualsAndHashCodeWithExistingMethods() {
    super();
  }
  public int hashCode() {
    return 42;
  }
}
final @lombok.EqualsAndHashCode class EqualsAndHashCodeWithExistingMethods2 {
  int x;
  EqualsAndHashCodeWithExistingMethods2() {
    super();
  }
  public boolean equals(Object other) {
    return false;
  }
}
final @lombok.EqualsAndHashCode(callSuper = true) class EqualsAndHashCodeWithExistingMethods3 extends EqualsAndHashCodeWithExistingMethods {
  int x;
  EqualsAndHashCodeWithExistingMethods3() {
    super();
  }
  public boolean canEqual(Object other) {
    return true;
  }
}