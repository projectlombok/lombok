import lombok.Builder;
class BuilderWithExistingBuilderClass<T, K extends Number> {
  public static class BuilderWithExistingBuilderClassBuilder<Z extends Number> {
    private @java.lang.SuppressWarnings("all") boolean arg2;
    private @java.lang.SuppressWarnings("all") String arg3;
    private Z arg1;
    public void arg2(boolean arg) {
    }
    @java.lang.SuppressWarnings("all") BuilderWithExistingBuilderClassBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder<Z> arg1(final Z arg1) {
      this.arg1 = arg1;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder<Z> arg3(final String arg3) {
      this.arg3 = arg3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithExistingBuilderClass<String, Z> build() {
      return BuilderWithExistingBuilderClass.<Z>staticMethod(this.arg1, this.arg2, this.arg3);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder(arg1=" + this.arg1) + ", arg2=") + this.arg2) + ", arg3=") + this.arg3) + ")");
    }
  }
  BuilderWithExistingBuilderClass() {
    super();
  }
  public static @Builder <Z extends Number>BuilderWithExistingBuilderClass<String, Z> staticMethod(Z arg1, boolean arg2, String arg3) {
    return null;
  }
  public static @java.lang.SuppressWarnings("all") <Z extends Number>BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder<Z> builder() {
    return new BuilderWithExistingBuilderClass.BuilderWithExistingBuilderClassBuilder<Z>();
  }
}