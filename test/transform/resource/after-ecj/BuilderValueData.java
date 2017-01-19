import java.util.List;
final @lombok.Builder @lombok.Value class BuilderAndValue {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated class BuilderAndValueBuilder {
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndValueBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndValue build() {
      return new BuilderAndValue();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
      return "BuilderAndValue.BuilderAndValueBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndValue() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndValueBuilder builder() {
    return new BuilderAndValueBuilder();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderAndValue)))
        return false;
    final BuilderAndValue other = (BuilderAndValue) o;
    if ((this.getZero() != other.getZero()))
        return false;
    return true;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
    return (("BuilderAndValue(zero=" + this.getZero()) + ")");
  }
}
@lombok.Builder @lombok.Data class BuilderAndData {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated class BuilderAndDataBuilder {
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndDataBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndData build() {
      return new BuilderAndData();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
      return "BuilderAndData.BuilderAndDataBuilder()";
    }
  }
  private final int zero = 0;
  @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndData() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderAndDataBuilder builder() {
    return new BuilderAndDataBuilder();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getZero() {
    return this.zero;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof BuilderAndData)))
        return false;
    final BuilderAndData other = (BuilderAndData) o;
    if ((! other.canEqual((java.lang.Object) this)))
        return false;
    if ((this.getZero() != other.getZero()))
        return false;
    return true;
  }
  protected @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated boolean canEqual(final java.lang.Object other) {
    return (other instanceof BuilderAndData);
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = ((result * PRIME) + this.getZero());
    return result;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
    return (("BuilderAndData(zero=" + this.getZero()) + ")");
  }
}