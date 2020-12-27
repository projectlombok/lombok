public class JacksonizedSuperBuilderSimple {
  public static @lombok.extern.jackson.Jacksonized @lombok.experimental.SuperBuilder @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true) @com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl.class) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends JacksonizedSuperBuilderSimple.Parent, B extends JacksonizedSuperBuilderSimple.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int field1;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("JacksonizedSuperBuilderSimple.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    static final @java.lang.SuppressWarnings("all") @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true) @com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "",buildMethodName = "build") class ParentBuilderImpl extends JacksonizedSuperBuilderSimple.Parent.ParentBuilder<JacksonizedSuperBuilderSimple.Parent, JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderSimple.Parent build() {
        return new JacksonizedSuperBuilderSimple.Parent(this);
      }
    }
    int field1;
    protected @java.lang.SuppressWarnings("all") Parent(final JacksonizedSuperBuilderSimple.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
    }
    public static @java.lang.SuppressWarnings("all") JacksonizedSuperBuilderSimple.Parent.ParentBuilder<?, ?> builder() {
      return new JacksonizedSuperBuilderSimple.Parent.ParentBuilderImpl();
    }
  }
  public JacksonizedSuperBuilderSimple() {
    super();
  }
  public static void test() {
    Parent x = Parent.builder().field1(5).build();
  }
}