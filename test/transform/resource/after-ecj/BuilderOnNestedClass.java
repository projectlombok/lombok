public class BuilderOnNestedClass<T> {
  public static @lombok.Builder class Nested {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated class NestedBuilder {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
      @java.lang.SuppressWarnings("all") @lombok.Generated NestedBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedClass.Nested.NestedBuilder a(final String a) {
        this.a = a;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedClass.Nested build() {
        return new BuilderOnNestedClass.Nested(this.a);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("BuilderOnNestedClass.Nested.NestedBuilder(a=" + this.a) + ")");
      }
    }
    private String a;
    @java.lang.SuppressWarnings("all") @lombok.Generated Nested(final String a) {
      super();
      this.a = a;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderOnNestedClass.Nested.NestedBuilder builder() {
      return new BuilderOnNestedClass.Nested.NestedBuilder();
    }
  }
  private T t;
  public BuilderOnNestedClass() {
    super();
  }
}
