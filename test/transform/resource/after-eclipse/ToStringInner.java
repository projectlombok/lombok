import lombok.ToString;
@ToString class ToStringOuter {
  @ToString class ToStringInner {
    final int y;
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringOuter.ToStringInner(y=" + this.y) + ")");
    }
    ToStringInner() {
      super();
    }
  }
  static @ToString class ToStringStaticInner {
    final int y;
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringOuter.ToStringStaticInner(y=" + this.y) + ")");
    }
    ToStringStaticInner() {
      super();
    }
  }
  class ToStringMiddle {
    @ToString class ToStringMoreInner {
      final String name;
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("ToStringOuter.ToStringMiddle.ToStringMoreInner(name=" + this.name) + ")");
      }
      ToStringMoreInner() {
        super();
      }
    }
    ToStringMiddle() {
      super();
    }
  }
  final int x;
  String name;
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ToStringOuter(x=" + this.x) + ", name=") + this.name) + ")");
  }
  ToStringOuter() {
    super();
  }
}
