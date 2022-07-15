public class SuperBuilderWithOverloadedGeneratedMethods {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends SuperBuilderWithOverloadedGeneratedMethods.Parent, B extends SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int self;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B self(final int self) {
        this.self = self;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder(self=" + this.self) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<SuperBuilderWithOverloadedGeneratedMethods.Parent, SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Parent build() {
        return new SuperBuilderWithOverloadedGeneratedMethods.Parent(this);
      }
    }
    int self;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<?, ?> b) {
      super();
      this.self = b.self;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends SuperBuilderWithOverloadedGeneratedMethods.Child, B extends SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double build;
      public ChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B build(final double build) {
        this.build = build;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder(super=" + super.toString()) + ", build=") + this.build) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl extends SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<SuperBuilderWithOverloadedGeneratedMethods.Child, SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Child build() {
        return new SuperBuilderWithOverloadedGeneratedMethods.Child(this);
      }
    }
    double build;
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.build = b.build;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithOverloadedGeneratedMethods() {
    super();
  }
  public static void test() {
    Child x = Child.builder().build(0.0).self(5).build();
  }
}
