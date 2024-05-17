import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
public @Builder class BuilderWithDeprecatedAnnOnly {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithDeprecatedAnnOnlyBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int dep1;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> strings;
    private @java.lang.SuppressWarnings("all") @lombok.Generated com.google.common.collect.ImmutableList.Builder<Integer> numbers;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnlyBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder dep1(final int dep1) {
      this.dep1 = dep1;
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder string(final String string) {
      if ((this.strings == null))
          this.strings = new java.util.ArrayList<String>();
      this.strings.add(string);
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder strings(final java.util.Collection<? extends String> strings) {
      if ((strings == null))
          {
            throw new java.lang.NullPointerException("strings cannot be null");
          }
      if ((this.strings == null))
          this.strings = new java.util.ArrayList<String>();
      this.strings.addAll(strings);
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder clearStrings() {
      if ((this.strings != null))
          this.strings.clear();
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder number(final Integer number) {
      if ((this.numbers == null))
          this.numbers = com.google.common.collect.ImmutableList.builder();
      this.numbers.add(number);
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder numbers(final java.lang.Iterable<? extends Integer> numbers) {
      if ((numbers == null))
          {
            throw new java.lang.NullPointerException("numbers cannot be null");
          }
      if ((this.numbers == null))
          this.numbers = com.google.common.collect.ImmutableList.builder();
      this.numbers.addAll(numbers);
      return this;
    }
    public @java.lang.Deprecated @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder clearNumbers() {
      this.numbers = null;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly build() {
      java.util.List<String> strings;
      switch (((this.strings == null) ? 0 : this.strings.size())) {
      case 0 :
          strings = java.util.Collections.emptyList();
          break;
      case 1 :
          strings = java.util.Collections.singletonList(this.strings.get(0));
          break;
      default :
          strings = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.strings));
      }
      com.google.common.collect.ImmutableList<Integer> numbers = ((this.numbers == null) ? com.google.common.collect.ImmutableList.<Integer>of() : this.numbers.build());
      return new BuilderWithDeprecatedAnnOnly(this.dep1, strings, numbers);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((((("BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder(dep1=" + this.dep1) + ", strings=") + this.strings) + ", numbers=") + this.numbers) + ")");
    }
  }
  @Deprecated int dep1;
  @Singular @Deprecated java.util.List<String> strings;
  @Singular @Deprecated ImmutableList<Integer> numbers;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly(final int dep1, final java.util.List<String> strings, final ImmutableList<Integer> numbers) {
    super();
    this.dep1 = dep1;
    this.strings = strings;
    this.numbers = numbers;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder builder() {
    return new BuilderWithDeprecatedAnnOnly.BuilderWithDeprecatedAnnOnlyBuilder();
  }
}
