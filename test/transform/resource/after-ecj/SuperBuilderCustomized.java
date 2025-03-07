import java.util.List;
public class SuperBuilderCustomized {
  public static @lombok.experimental.SuperBuilder class Parent<T> {
    public static abstract class ParentBuilder<T, C extends Parent<T>, B extends ParentBuilder<T, C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      public ParentBuilder() {
        super();
      }
      public B resetToDefault() {
        field1 = 0;
        return self();
      }
      public B field1(int field1) {
        this.field1 = (field1 + 1);
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderCustomized.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl<T> extends SuperBuilderCustomized.Parent.ParentBuilder<T, SuperBuilderCustomized.Parent<T>, SuperBuilderCustomized.Parent.ParentBuilderImpl<T>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomized.Parent.ParentBuilderImpl<T> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomized.Parent<T> build() {
        return new SuperBuilderCustomized.Parent<T>(this);
      }
    }
    int field1;
    protected Parent(ParentBuilder<T, ?, ?> b) {
      super();
      if ((b.field1 == 0))
          throw new IllegalArgumentException("field1 must be != 0");
      this.field1 = b.field1;
    }
    public static <T>SuperBuilderCustomized.Parent.ParentBuilder<T, ?, ?> builder(int field1) {
      return new SuperBuilderCustomized.Parent.ParentBuilderImpl<T>().field1(field1);
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent<String> {
    private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      public @Override Child build() {
        this.resetToDefault();
        return new Child(this);
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomized.Child.ChildBuilderImpl self() {
        return this;
      }
    }
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderCustomized.Child, B extends SuperBuilderCustomized.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<String, C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double field2;
      public ChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field2(final double field2) {
        this.field2 = field2;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderCustomized.Child.ChildBuilder(super=" + super.toString()) + ", field2=") + this.field2) + ")");
      }
    }
    double field2;
    public static ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl().field2(10.0);
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderCustomized.Child.ChildBuilder<?, ?> b) {
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
