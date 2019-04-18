import lombok.ToString;
@lombok.ToString class ToString1 {
  int x;
  String name;
  ToString1() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ToString1(x=" + this.x) + ", name=") + this.name) + ")");
  }
}
@ToString class ToString2 {
  int x;
  String name;
  ToString2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ToString2(x=" + this.x) + ", name=") + this.name) + ")");
  }
}
