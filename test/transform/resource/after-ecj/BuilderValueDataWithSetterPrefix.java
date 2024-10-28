import java.util.List;
final @lombok.Builder(setterPrefix = "with") @lombok.Value class BuilderAndValueWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderAndValueWithSetterPrefixBuilder {
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndValueWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndValueWithSetterPrefix build() {
      return new BuilderAndValueWithSetterPrefix();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return "BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndValueWithSetterPrefix() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder builder() {
    return new BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderAndValueWithSetterPrefix)))
        return false;
    final BuilderAndValueWithSetterPrefix other = (BuilderAndValueWithSetterPrefix) o;
    if ((this.getZero() != other.getZero()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("BuilderAndValueWithSetterPrefix(zero=" + this.getZero()) + ")");
  }
}
@lombok.Builder(setterPrefix = "with") @lombok.Data class BuilderAndDataWithSetterPrefix {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderAndDataWithSetterPrefixBuilder {
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndDataWithSetterPrefixBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndDataWithSetterPrefix build() {
      return new BuilderAndDataWithSetterPrefix();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return "BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndDataWithSetterPrefix() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder builder() {
    return new BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
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
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof BuilderAndDataWithSetterPrefix);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("BuilderAndDataWithSetterPrefix(zero=" + this.getZero()) + ")");
  }
}
