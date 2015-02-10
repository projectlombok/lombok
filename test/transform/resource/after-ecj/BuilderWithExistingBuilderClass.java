import lombok.Builder;
class BuilderWithExistingBuilderClass<T, K extends Number> {
  public static class BuilderWithExistingBuilderClassBuilder<Z extends Number> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") boolean arg2;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String arg3;
    private Z arg1;
    public void arg2(boolean arg) {
    }
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithExistingBuilderClassBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithExistingBuilderClassBuilder<Z> arg1(final Z arg1) {
      this.arg1 = arg1;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithExistingBuilderClassBuilder<Z> arg3(final String arg3) {
      this.arg3 = arg3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithExistingBuilderClass<String, Z> build() {
      return BuilderWithExistingBuilderClass.<Z>staticMethod(arg1, arg2, arg3);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((("BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder(arg1=" + this.arg1) + ", arg2=") + this.arg2) + ", arg3=") + this.arg3) + ")");
    }
  }
  BuilderWithExistingBuilderClass() {
    super();
  }
  public static @Builder <Z extends Number>BuilderWithExistingBuilderClass<String, Z> staticMethod(Z arg1, boolean arg2, String arg3) {
    return null;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <Z extends Number>BuilderWithExistingBuilderClassBuilder<Z> builder() {
    return new BuilderWithExistingBuilderClassBuilder<Z>();
  }
}