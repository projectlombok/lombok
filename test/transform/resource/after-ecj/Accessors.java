class AccessorsFluent {
  private @lombok.Getter @lombok.Setter @lombok.experimental.Accessors(fluent = true) String fieldName = "";
  public @java.lang.SuppressWarnings("all") String fieldName() {
    return this.fieldName;
  }
  public @java.lang.SuppressWarnings("all") AccessorsFluent fieldName(final String fieldName) {
    this.fieldName = fieldName;
    return this;
  }
  AccessorsFluent() {
    super();
  }
}
@lombok.experimental.Accessors(fluent = true) @lombok.Getter class AccessorsFluentOnClass {
  private @lombok.Setter String fieldName = "";
  private @lombok.experimental.Accessors String otherFieldWithOverride = "";
  public @java.lang.SuppressWarnings("all") AccessorsFluentOnClass fieldName(final String fieldName) {
    this.fieldName = fieldName;
    return this;
  }
  public @java.lang.SuppressWarnings("all") String fieldName() {
    return this.fieldName;
  }
  public @java.lang.SuppressWarnings("all") String getOtherFieldWithOverride() {
    return this.otherFieldWithOverride;
  }
  AccessorsFluentOnClass() {
    super();
  }
}
class AccessorsChain {
  private @lombok.Setter @lombok.experimental.Accessors(chain = true) boolean isRunning;
  public @java.lang.SuppressWarnings("all") AccessorsChain setRunning(final boolean isRunning) {
    this.isRunning = isRunning;
    return this;
  }
  AccessorsChain() {
    super();
  }
}
@lombok.experimental.Accessors(prefix = "f") class AccessorsPrefix {
  private @lombok.Setter String fieldName;
  private @lombok.Setter String fActualField;
  public @java.lang.SuppressWarnings("all") void setActualField(final String fActualField) {
    this.fActualField = fActualField;
  }
  AccessorsPrefix() {
    super();
  }
}
@lombok.experimental.Accessors(prefix = {"f", ""}) class AccessorsPrefix2 {
  private @lombok.Setter String fieldName;
  private @lombok.Setter String fActualField;
  public @java.lang.SuppressWarnings("all") void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }
  public @java.lang.SuppressWarnings("all") void setActualField(final String fActualField) {
    this.fActualField = fActualField;
  }
  AccessorsPrefix2() {
    super();
  }
}
@lombok.experimental.Accessors(prefix = "f") @lombok.ToString @lombok.EqualsAndHashCode class AccessorsPrefix3 {
  private String fName;
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("AccessorsPrefix3(fName=" + this.getName()) + ")");
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof AccessorsPrefix3)))
        return false;
    final @java.lang.SuppressWarnings("all") AccessorsPrefix3 other = (AccessorsPrefix3) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if (((this.getName() == null) ? (other.getName() != null) : (! this.getName().equals((java.lang.Object) other.getName()))))
        return false;
    return true;
  }
  public @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof AccessorsPrefix3);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = ((result * PRIME) + ((this.getName() == null) ? 0 : this.getName().hashCode()));
    return result;
  }
  AccessorsPrefix3() {
    super();
  }
  private String getName() {
    return fName;
  }
}
class AccessorsFluentGenerics<T extends Number> {
  private @lombok.Setter @lombok.experimental.Accessors(fluent = true) String name;
  public @java.lang.SuppressWarnings("all") AccessorsFluentGenerics<T> name(final String name) {
    this.name = name;
    return this;
  }
  AccessorsFluentGenerics() {
    super();
  }
}
class AccessorsFluentNoChaining {
  private @lombok.Setter @lombok.experimental.Accessors(fluent = true,chain = false) String name;
  public @java.lang.SuppressWarnings("all") void name(final String name) {
    this.name = name;
  }
  AccessorsFluentNoChaining() {
    super();
  }
}
class AccessorsFluentStatic<T extends Number> {
  private static @lombok.Setter @lombok.experimental.Accessors(fluent = true) String name;
  <clinit>() {
  }
  public static @java.lang.SuppressWarnings("all") void name(final String name) {
    AccessorsFluentStatic.name = name;
  }
  AccessorsFluentStatic() {
    super();
  }
}
