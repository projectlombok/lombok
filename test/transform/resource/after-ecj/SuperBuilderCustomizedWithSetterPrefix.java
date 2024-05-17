import java.util.List;
public class SuperBuilderCustomizedWithSetterPrefix {
  public static @lombok.experimental.SuperBuilder(setterPrefix = "set") class Parent {
    public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      public ParentBuilder() {
        super();
      }
      public B setField1(int field1) {
        this.field1 = (field1 + 1);
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder(field1=" + this.field1) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<SuperBuilderCustomizedWithSetterPrefix.Parent, SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomizedWithSetterPrefix.Parent build() {
        return new SuperBuilderCustomizedWithSetterPrefix.Parent(this);
      }
    }
    int field1;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderCustomizedWithSetterPrefix.Parent.ParentBuilderImpl();
    }
  }
  public SuperBuilderCustomizedWithSetterPrefix() {
    super();
  }
  public static void test() {
    Parent x = Parent.builder().setField1(5).build();
  }
}
