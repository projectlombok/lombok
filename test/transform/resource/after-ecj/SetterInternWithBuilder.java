import lombok.Setter;
import lombok.Builder;
public @Builder class SetterInternWithBuilder {
  public static @java.lang.SuppressWarnings("all") class SetterInternWithBuilderBuilder {
    private @java.lang.SuppressWarnings("all") String str1;
    @java.lang.SuppressWarnings("all") SetterInternWithBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") SetterInternWithBuilder.SetterInternWithBuilderBuilder str1(final String str1) {
      this.str1 = str1;
      return this;
    }
    public @java.lang.SuppressWarnings("all") SetterInternWithBuilder build() {
      return new SetterInternWithBuilder(this.str1);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("SetterInternWithBuilder.SetterInternWithBuilderBuilder(str1=" + this.str1) + ")");
    }
  }
  private @Setter(internString = true) String str1;
  @java.lang.SuppressWarnings("all") SetterInternWithBuilder(final String str1) {
    super();
    this.str1 = str1;
  }
  public static @java.lang.SuppressWarnings("all") SetterInternWithBuilder.SetterInternWithBuilderBuilder builder() {
    return new SetterInternWithBuilder.SetterInternWithBuilderBuilder();
  }
  public @java.lang.SuppressWarnings("all") void setStr1(final String str1) {
    this.str1 = ((str1 == null) ? null : str1.intern());
  }
}