import java.util.List;
public class SuperBuilderCustomized {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int field1;
      public ParentBuilder() {
        super();
      }
      public B resetToDefault() {
        field1 = 0;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderCustomized.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends SuperBuilderCustomized.Parent.ParentBuilder<SuperBuilderCustomized.Parent, SuperBuilderCustomized.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderCustomized.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderCustomized.Parent build() {
        return new SuperBuilderCustomized.Parent(this);
      }
    }
    int field1;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderCustomized.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderCustomized.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderCustomized.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      public @Override Child build() {
        this.resetToDefault();
        return new Child(this);
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderCustomized.Child.ChildBuilderImpl self() {
        return this;
      }
    }
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends SuperBuilderCustomized.Child, B extends SuperBuilderCustomized.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double field2;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field2(final double field2) {
        this.field2 = field2;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderCustomized.Child.ChildBuilder(super=" + super.toString()) + ", field2=") + this.field2) + ")");
      }
    }
    double field2;
    public static ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl().field2(10.0);
    }
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderCustomized.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field2 = b.field2;
    }
  }
  public SuperBuilderCustomized() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field2(1.0).field1(5).resetToDefault().build();
  }
}
