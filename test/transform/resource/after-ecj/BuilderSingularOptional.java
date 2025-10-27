import java.util.Optional;
import lombok.Singular;
@lombok.Builder class BuilderSingularOptional<T> {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularOptionalBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated T value$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name$value;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptionalBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> value(final T value) {
      this.value$value = value;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearValue() {
      this.value$value = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> name(final String name) {
      this.name$value = name;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearName() {
      this.name$value = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional<T> build() {
      java.util.Optional<T> value = java.util.Optional.ofNullable(this.value$value);
      java.util.Optional<String> name = java.util.Optional.ofNullable(this.name$value);
      return new BuilderSingularOptional<T>(value, name);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderSingularOptional.BuilderSingularOptionalBuilder(value$value=" + this.value$value) + ", name$value=") + this.name$value) + ")");
    }
  }
  private @Singular Optional<T> value;
  private @Singular Optional<String> name;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional(final Optional<T> value, final Optional<String> name) {
    super();
    this.value = value;
    this.name = name;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated <T>BuilderSingularOptional.BuilderSingularOptionalBuilder<T> builder() {
    return new BuilderSingularOptional.BuilderSingularOptionalBuilder<T>();
  }
}
