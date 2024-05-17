import java.util.List;
import lombok.Builder;
import lombok.Singular;
@Builder class CheckerFrameworkBuilder {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class CheckerFrameworkBuilderBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int x$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean x$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
    private @java.lang.SuppressWarnings("all") @lombok.Generated int z;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> names;
    @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder x(final int x) {
      this.x$value = x;
      x$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder y(final int y) {
      this.y = y;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder z(final int z) {
      this.z = z;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder name(final String name) {
      if ((this.names == null))
          this.names = new java.util.ArrayList<String>();
      this.names.add(name);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder names(final java.util.Collection<? extends String> names) {
      if ((names == null))
          {
            throw new java.lang.NullPointerException("names cannot be null");
          }
      if ((this.names == null))
          this.names = new java.util.ArrayList<String>();
      this.names.addAll(names);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.returnsreceiver.qual.This CheckerFrameworkBuilderBuilder clearNames() {
      if ((this.names != null))
          this.names.clear();
      return this;
    }
    public @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder build(CheckerFrameworkBuilder.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) CheckerFrameworkBuilderBuilder this) {
      java.util.List<String> names;
      switch (((this.names == null) ? 0 : this.names.size())) {
      case 0 :
          names = java.util.Collections.emptyList();
          break;
      case 1 :
          names = java.util.Collections.singletonList(this.names.get(0));
          break;
      default :
          names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.names));
      }
      int x$value = this.x$value;
      if ((! this.x$set))
          x$value = CheckerFrameworkBuilder.$default$x();
      return new CheckerFrameworkBuilder(x$value, this.y, this.z, names);
    }
    public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((((("CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ", z=") + this.z) + ", names=") + this.names) + ")");
    }
  }
  @Builder.Default int x;
  int y;
  int z;
  @Singular List<String> names;
  private static @java.lang.SuppressWarnings("all") @lombok.Generated int $default$x() {
    return 5;
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder(final int x, final int y, final int z, final List<String> names) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
    this.names = names;
  }
  public static @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") @lombok.Generated CheckerFrameworkBuilder.@org.checkerframework.common.aliasing.qual.Unique CheckerFrameworkBuilderBuilder builder() {
    return new CheckerFrameworkBuilder.CheckerFrameworkBuilderBuilder();
  }
}
