import lombok.ToString;
@ToString enum ToStringEnum1 {
  CONSTANT(),
  <clinit>() {
  }
  ToStringEnum1() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return ("ToStringEnum1." + this.name());
  }
}
@ToString enum ToStringEnum2 {
  CONSTANT(),
  int x;
  String name;
  <clinit>() {
  }
  ToStringEnum2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("ToStringEnum2." + this.name()) + "(x=") + this.x) + ", name=") + this.name) + ")");
  }
}
class ToStringEnum3 {
  @ToString enum MemberEnum {
    CONSTANT(),
    <clinit>() {
    }
    MemberEnum() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return ("ToStringEnum3.MemberEnum." + this.name());
    }
  }
  ToStringEnum3() {
    super();
  }
}
