import java.util.List;
final @lombok.Builder(setterPrefix = "with") @lombok.Value class BuilderAndValueWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderAndValueWithSetterPrefixBuilder {
    @java.lang.SuppressWarnings("all") BuilderAndValueWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderAndValueWithSetterPrefix build() {
      return new BuilderAndValueWithSetterPrefix();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return "BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") BuilderAndValueWithSetterPrefix() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder builder() {
    return new BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderAndValueWithSetterPrefix)))
        return false;
    final BuilderAndValueWithSetterPrefix other = (BuilderAndValueWithSetterPrefix) o;
    if ((this.getZero() != other.getZero()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("BuilderAndValueWithSetterPrefix(zero=" + this.getZero()) + ")");
  }
}
@lombok.Builder(setterPrefix = "with") @lombok.Data class BuilderAndDataWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") class BuilderAndDataWithSetterPrefixBuilder {
    @java.lang.SuppressWarnings("all") BuilderAndDataWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderAndDataWithSetterPrefix build() {
      return new BuilderAndDataWithSetterPrefix();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return "BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") BuilderAndDataWithSetterPrefix() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder builder() {
    return new BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderAndDataWithSetterPrefix)))
        return false;
    final BuilderAndDataWithSetterPrefix other = (BuilderAndDataWithSetterPrefix) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getZero() != other.getZero()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") boolean canEqual(final java.lang.Object other) {
    return (other instanceof BuilderAndDataWithSetterPrefix);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("BuilderAndDataWithSetterPrefix(zero=" + this.getZero()) + ")");
  }
}
