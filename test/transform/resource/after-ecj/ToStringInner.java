import lombok.ToString;
@ToString class ToStringOuter {
  @ToString class ToStringInner {
    int y;
    ToStringInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringOuter.ToStringInner(y=" + this.y) + ")");
    }
  }
  static @ToString class ToStringStaticInner {
    int y;
    ToStringStaticInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringOuter.ToStringStaticInner(y=" + this.y) + ")");
    }
  }
  class ToStringMiddle {
    @ToString class ToStringMoreInner {
      String name;
      ToStringMoreInner() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("ToStringOuter.ToStringMiddle.ToStringMoreInner(name=" + this.name) + ")");
      }
    }
    ToStringMiddle() {
      super();
    }
  }
  int x;
  String name;
  ToStringOuter() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ToStringOuter(x=" + this.x) + ", name=") + this.name) + ")");
  }
}
