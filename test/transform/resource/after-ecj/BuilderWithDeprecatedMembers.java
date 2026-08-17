//version 9:
import lombok.Builder;
import lombok.Singular;
public @Builder class BuilderWithDeprecatedMembers {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithDeprecatedMembersBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated int both;
    private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> strings;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembersBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.Deprecated(since = "1.2",forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder both(final int both) {
      this.both = both;
      return this;
    }
    public @java.lang.Deprecated(forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder string(final String string) {
      if ((this.strings == null))
          this.strings = new java.util.ArrayList<String>();
      this.strings.add(string);
      return this;
    }
    public @java.lang.Deprecated(forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder strings(final java.util.Collection<? extends String> strings) {
      if ((strings == null))
          {
            throw new java.lang.NullPointerException("strings cannot be null");
          }
      if ((this.strings == null))
          this.strings = new java.util.ArrayList<String>();
      this.strings.addAll(strings);
      return this;
    }
    public @java.lang.Deprecated(forRemoval = true) @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder clearStrings() {
      if ((this.strings != null))
          this.strings.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers build() {
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
      return new BuilderWithDeprecatedMembers(this.both, strings);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder(both=" + this.both) + ", strings=") + this.strings) + ")");
    }
  }
  @Deprecated(since = "1.2",forRemoval = true) int both;
  @Singular @Deprecated(forRemoval = true) java.util.List<String> strings;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers(final int both, final java.util.List<String> strings) {
    super();
    this.both = both;
    this.strings = strings;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder builder() {
    return new BuilderWithDeprecatedMembers.BuilderWithDeprecatedMembersBuilder();
  }
}
