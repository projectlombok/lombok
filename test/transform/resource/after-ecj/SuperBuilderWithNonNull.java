import java.util.List;
public class SuperBuilderWithNonNull {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") String nonNullParentField;
      private @java.lang.SuppressWarnings("all") boolean nonNullParentField$set;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B nonNullParentField(final @lombok.NonNull String nonNullParentField) {
        this.nonNullParentField = nonNullParentField;
        nonNullParentField$set = true;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderWithNonNull.Parent.ParentBuilder(nonNullParentField=" + this.nonNullParentField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends ParentBuilder<Parent, ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") Parent build() {
        return new Parent(this);
      }
    }
    final @lombok.NonNull @lombok.Builder.Default String nonNullParentField;
    private static @java.lang.SuppressWarnings("all") String $default$nonNullParentField() {
      return "default";
    }
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<?, ?> b) {
      super();
      if (b.nonNullParentField$set)
          this.nonNullParentField = b.nonNullParentField;
      else
          this.nonNullParentField = Parent.$default$nonNullParentField();
      if ((nonNullParentField == null))
          {
            throw new java.lang.NullPointerException("nonNullParentField is marked @NonNull but is null");
          }
    }
    public static @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
      return new ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") String nonNullChildField;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B nonNullChildField(final @lombok.NonNull String nonNullChildField) {
        this.nonNullChildField = nonNullChildField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithNonNull.Child.ChildBuilder(super=" + super.toString()) + ", nonNullChildField=") + this.nonNullChildField) + ")");
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
    @lombok.NonNull String nonNullChildField;
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.nonNullChildField = b.nonNullChildField;
      if ((nonNullChildField == null))
          {
            throw new java.lang.NullPointerException("nonNullChildField is marked @NonNull but is null");
          }
    }
    public static @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }
  public SuperBuilderWithNonNull() {
    super();
  }
  public static void test() {
    Child x = Child.builder().nonNullChildField("child").nonNullParentField("parent").build();
  }
}
