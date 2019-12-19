import java.util.List;
public class SuperBuilderWithGenerics3 {
  public static @lombok.experimental.SuperBuilder class Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<A, C extends Parent<A>, B extends ParentBuilder<A, C, B>> {
      private @java.lang.SuppressWarnings("all") String str;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B str(final String str) {
        this.str = str;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderWithGenerics3.Parent.ParentBuilder(str=" + this.str) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl<A> extends ParentBuilder<A, Parent<A>, ParentBuilderImpl<A>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ParentBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Parent<A> build() {
        return new Parent<A>(this);
      }
    }
    private final String str;
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<A, ?, ?> b) {
      super();
      this.str = b.str;
    }
    public static @java.lang.SuppressWarnings("all") <A>ParentBuilder<A, ?, ?> builder() {
      return new ParentBuilderImpl<A>();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<Child.SomeInnerStaticClass> {
    public static class SomeInnerStaticClass {
      public SomeInnerStaticClass() {
        super();
      }
    }
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<Child.SomeInnerStaticClass, C, B> {
      private @java.lang.SuppressWarnings("all") double field3;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field3(final double field3) {
        this.field3 = field3;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithGenerics3.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Child build() {
        return new Child(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }
  public SuperBuilderWithGenerics3() {
    super();
  }
}
