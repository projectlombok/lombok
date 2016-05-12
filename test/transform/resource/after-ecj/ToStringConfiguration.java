import lombok.ToString;
import lombok.Getter;
@ToString @Getter class ToStringConfiguration {
  int x;
  ToStringConfiguration() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringConfiguration(" + this.x) + ")");
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int getX() {
    return this.x;
  }
}
@ToString(includeFieldNames = true) class ToStringConfiguration2 {
  int x;
  ToStringConfiguration2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringConfiguration2(x=" + this.x) + ")");
  }
}
@ToString(doNotUseGetters = false) @Getter class ToStringConfiguration3 {
  int x;
  ToStringConfiguration3() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringConfiguration3(" + this.getX()) + ")");
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int getX() {
    return this.x;
  }
}
@ToString(counts = true,includeFieldNames = true) class ToStringConfiguration4 {
  java.lang.String[] array;
  java.util.List<java.lang.String> strings;
  java.util.Set<java.lang.Integer> set;
  java.util.Map<java.lang.String, java.lang.Integer> map;
  int x;
  ToStringConfiguration4() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (((((((((("ToStringConfiguration4(array=" + java.util.Arrays.deepToString(this.array)) + ", strings=") + this.strings) + ", set=") + this.set) + ", map=") + this.map) + ", x=") + this.x) + ")");
  }
}