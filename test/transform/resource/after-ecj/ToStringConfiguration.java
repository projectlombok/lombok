import lombok.ToString;
import lombok.Getter;
@ToString @Getter class ToStringConfiguration {
  int x;
  ToStringConfiguration() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("ToStringConfiguration(" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
}
@ToString(includeFieldNames = true) class ToStringConfiguration2 {
  int x;
  ToStringConfiguration2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("ToStringConfiguration2(x=" + this.x) + ")");
  }
}
@ToString(doNotUseGetters = false) @Getter class ToStringConfiguration3 {
  int x;
  ToStringConfiguration3() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("ToStringConfiguration3(" + this.getX()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getX() {
    return this.x;
  }
}
