import lombok.ToString;
public @ToString class ToStringNewStyle {
  @ToString.Include(name = "a") int b;
  double c;
  int f;
  @ToString.Include(name = "e") int d;
  int g;
  @ToString.Include(rank = (- 1)) int h;
  int i;
  @ToString.Exclude int j;
  public ToStringNewStyle() {
    super();
  }
  @ToString.Include int f() {
    return 0;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((((((((((("ToStringNewStyle(a=" + this.b) + ", c=") + this.c) + ", e=") + this.d) + ", f=") + this.f()) + ", g=") + this.g) + ", i=") + this.i) + ", h=") + this.h) + ")");
  }
}
