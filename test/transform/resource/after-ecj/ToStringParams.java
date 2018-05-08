package lombok.test;
import lombok.ToString;
@ToString(fqn = true, prefix = "\n{\n    \"", separator = "\" : \"", infix = "\",\n    \"", suffix = "\"\n}")
class ToStringOuter {
  @ToString(fqn = true)
  class ToStringInner {
    int y;
    ToStringInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("lombok.test.ToStringOuter.ToStringInner(y=" + this.y) + ")");
    }
  }
  @ToString(separator = " = ")
  static class ToStringStaticInner {
    int y;
    ToStringStaticInner() {
      super();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("ToStringOuter.ToStringStaticInner(y = " + this.y) + ")");
    }
  }
  class ToStringMiddle {
    @ToString(fqn = true, prefix = "[\n\t", callSuper = true, includeFieldNames = false, separator = " = ", infix = ",\n\t", suffix = "\n]")
    class ToStringMoreInner extends ToStringInner {
      String name;
      ToStringMoreInner() {
        super();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("lombok.test.ToStringOuter.ToStringMiddle.ToStringMoreInner[\n\tsuper = " + super.toString()) + ",\n\t") + this.name) + "\n]");
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
    return (((("lombok.test.ToStringOuter\n{\n    \"x\" : \"" + this.x) + "\",\n    \"name\" : \"") + this.name) + "\"\n}");
  }
}
