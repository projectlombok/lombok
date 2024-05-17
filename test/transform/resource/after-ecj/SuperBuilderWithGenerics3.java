import java.util.List;
public class SuperBuilderWithGenerics3 {
  public static @lombok.experimental.SuperBuilder class Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<A, C extends SuperBuilderWithGenerics3.Parent<A>, B extends SuperBuilderWithGenerics3.Parent.ParentBuilder<A, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String str;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B str(final String str) {
        this.str = str;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderWithGenerics3.Parent.ParentBuilder(str=" + this.str) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl<A> extends SuperBuilderWithGenerics3.Parent.ParentBuilder<A, SuperBuilderWithGenerics3.Parent<A>, SuperBuilderWithGenerics3.Parent.ParentBuilderImpl<A>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithGenerics3.Parent.ParentBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithGenerics3.Parent<A> build() {
        return new SuperBuilderWithGenerics3.Parent<A>(this);
      }
    }
    private final String str;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithGenerics3.Parent.ParentBuilder<A, ?, ?> b) {
      super();
      this.str = b.str;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <A>SuperBuilderWithGenerics3.Parent.ParentBuilder<A, ?, ?> builder() {
      return new SuperBuilderWithGenerics3.Parent.ParentBuilderImpl<A>();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<Child.SomeInnerStaticClass> {
    public static class SomeInnerStaticClass {
      public SomeInnerStaticClass() {
        super();
      }
    }
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithGenerics3.Child, B extends SuperBuilderWithGenerics3.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Child.SomeInnerStaticClass, C, B> {
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
        return (((("SuperBuilderWithGenerics3.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithGenerics3.Child.ChildBuilder<SuperBuilderWithGenerics3.Child, SuperBuilderWithGenerics3.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithGenerics3.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithGenerics3.Child build() {
        return new SuperBuilderWithGenerics3.Child(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithGenerics3.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithGenerics3.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithGenerics3.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithGenerics3() {
    super();
  }
}
