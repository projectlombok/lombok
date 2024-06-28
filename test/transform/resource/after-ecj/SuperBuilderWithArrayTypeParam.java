//version 8:
public class SuperBuilderWithArrayTypeParam {
  public static @lombok.experimental.SuperBuilder class Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<A, C extends SuperBuilderWithArrayTypeParam.Parent<A>, B extends SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated A field1;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field1(final A field1) {
        this.field1 = field1;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderWithArrayTypeParam.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl<A> extends SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, SuperBuilderWithArrayTypeParam.Parent<A>, SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithArrayTypeParam.Parent<A> build() {
        return new SuperBuilderWithArrayTypeParam.Parent<A>(this);
      }
    }
    A field1;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, ?, ?> b) {
      super();
      this.field1 = b.field1;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <A>SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, ?, ?> builder() {
      return new SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A>();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<Integer[]> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithArrayTypeParam.Child, B extends SuperBuilderWithArrayTypeParam.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer[], C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double field3;
      public ChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field3(final double field3) {
        this.field3 = field3;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithArrayTypeParam.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithArrayTypeParam.Child.ChildBuilder<SuperBuilderWithArrayTypeParam.Child, SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithArrayTypeParam.Child build() {
        return new SuperBuilderWithArrayTypeParam.Child(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithArrayTypeParam.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithArrayTypeParam.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithArrayTypeParam() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field3(0.0).field1(new Integer[]{2}).build();
  }
}
