import lombok.ToString;
@ToString class ToStringOuter {
  @ToString class ToStringInner {
    final int y;
    ToStringInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringInner(y=" + this.y) + ")");
    }
  }
  static @ToString class ToStringStaticInner {
    final int y;
    ToStringStaticInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringStaticInner(y=" + this.y) + ")");
    }
  }
  class ToStringMiddle {
    @ToString class ToStringMoreInner {
      final String name;
      ToStringMoreInner() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("ToStringMoreInner(name=" + this.name) + ")");
      }
    }
    ToStringMiddle() {
      super();
    }
  }
  final int x;
  String name;
  ToStringOuter() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
    return (((("ToStringOuter(x=" + this.x) + ", name=") + this.name) + ")");
  }
}
