@lombok.ToString class ToStringAutoSuperWithNoParent {
  ToStringAutoSuperWithNoParent() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return "ToStringAutoSuperWithNoParent()";
  }
}
@lombok.ToString class ToStringAutoSuperWithParent extends ToStringAutoSuperWithNoParent {
  ToStringAutoSuperWithParent() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (("ToStringAutoSuperWithParent(super=" + super.toString()) + ")");
  }
}
