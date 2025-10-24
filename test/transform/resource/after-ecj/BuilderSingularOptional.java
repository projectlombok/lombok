import java.util.Optional;
import lombok.Singular;
@lombok.Builder class BuilderSingularOptional<T> {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderSingularOptionalBuilder<T> {
    private @java.lang.SuppressWarnings("all") @lombok.Generated T value$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean value$set;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String name$value;
    private @java.lang.SuppressWarnings("all") @lombok.Generated boolean name$set;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptionalBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> value(final T value) {
      this.value$value = value;
      this.value$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearValue() {
      this.value$set = false;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> name(final String name) {
      this.name$value = name;
      this.name$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearName() {
      this.name$set = false;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderSingularOptional<T> build() {
      java.util.Optional<T> value;
      if (this.value$set)
        value = java.util.Optional.ofNullable(this.value$value);
      else
        value = java.util.Optional.empty();
      java.util.Optional<String> name;
      if (this.name$set)
        name = java.util.Optional.ofNullable(this.name$value);
      else
        name = java.util.Optional.empty();
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
