import lombok.Builder;
class BuilderWithExistingBuilderClassWithSetterPrefix<T, K extends Number> {
  public static class BuilderWithExistingBuilderClassWithSetterPrefixBuilder<Z extends Number> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean arg2;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String arg3;
    private Z arg1;
    public void withArg2(boolean arg) {
    }
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithExistingBuilderClassWithSetterPrefixBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithExistingBuilderClassWithSetterPrefix.BuilderWithExistingBuilderClassWithSetterPrefixBuilder<Z> withArg1(final Z arg1) {
      this.arg1 = arg1;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithExistingBuilderClassWithSetterPrefix.BuilderWithExistingBuilderClassWithSetterPrefixBuilder<Z> withArg3(final String arg3) {
      this.arg3 = arg3;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithExistingBuilderClassWithSetterPrefix<String, Z> build() {
      return BuilderWithExistingBuilderClassWithSetterPrefix.<Z>staticMethod(this.arg1, this.arg2, this.arg3);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderWithExistingBuilderClassWithSetterPrefix.BuilderWithExistingBuilderClassWithSetterPrefixBuilder(arg1=" + this.arg1) + ", arg2=") + this.arg2) + ", arg3=") + this.arg3) + ")");
    }
  }
  BuilderWithExistingBuilderClassWithSetterPrefix() {
    super();
  }
  public static @Builder(setterPrefix = "with") <Z extends Number>BuilderWithExistingBuilderClassWithSetterPrefix<String, Z> staticMethod(Z arg1, boolean arg2, String arg3) {
    return null;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <Z extends Number>BuilderWithExistingBuilderClassWithSetterPrefix.BuilderWithExistingBuilderClassWithSetterPrefixBuilder<Z> builder() {
    return new BuilderWithExistingBuilderClassWithSetterPrefix.BuilderWithExistingBuilderClassWithSetterPrefixBuilder<Z>();
  }
}
