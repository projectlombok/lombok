public @lombok.Builder class BuilderNestedJavadoc {
  public static @lombok.Builder class NestedClass {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated class NestedClassBuilder {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String name;
      @java.lang.SuppressWarnings("all") @lombok.Generated NestedClassBuilder() {
        super();
      }
      /**
       * Example javadoc
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc.NestedClass.NestedClassBuilder name(final String name) {
        this.name = name;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc.NestedClass build() {
        return new BuilderNestedJavadoc.NestedClass(this.name);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("BuilderNestedJavadoc.NestedClass.NestedClassBuilder(name=" + this.name) + ")");
      }
    }
    String name;
    /**
     * Creates a new {@code NestedClass} instance.
     *
     * @param name Example javadoc
     */
    @java.lang.SuppressWarnings("all") @lombok.Generated NestedClass(final String name) {
      super();
      this.name = name;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc.NestedClass.NestedClassBuilder builder() {
      return new BuilderNestedJavadoc.NestedClass.NestedClassBuilder();
    }
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderNestedJavadocBuilder {
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadocBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc build() {
      return new BuilderNestedJavadoc();
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return "BuilderNestedJavadoc.BuilderNestedJavadocBuilder()";
    }
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderNestedJavadoc.BuilderNestedJavadocBuilder builder() {
    return new BuilderNestedJavadoc.BuilderNestedJavadocBuilder();
  }
}
