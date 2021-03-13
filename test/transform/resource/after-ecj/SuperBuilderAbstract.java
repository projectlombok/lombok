public class SuperBuilderAbstract {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends SuperBuilderAbstract.Parent, B extends SuperBuilderAbstract.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int parentField;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B parentField(final int parentField) {
        this.parentField = parentField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderAbstract.Parent.ParentBuilder(parentField=" + this.parentField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends SuperBuilderAbstract.Parent.ParentBuilder<SuperBuilderAbstract.Parent, SuperBuilderAbstract.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderAbstract.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderAbstract.Parent build() {
        return new SuperBuilderAbstract.Parent(this);
      }
    }
    int parentField;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderAbstract.Parent.ParentBuilder<?, ?> b) {
      super();
      this.parentField = b.parentField;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderAbstract.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderAbstract.Parent.ParentBuilderImpl();
    }
  }
  public static abstract @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends SuperBuilderAbstract.Child, B extends SuperBuilderAbstract.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double childField;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B childField(final double childField) {
        this.childField = childField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstract.Child.ChildBuilder(super=" + super.toString()) + ", childField=") + this.childField) + ")");
      }
    }
    double childField;
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderAbstract.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.childField = b.childField;
    }
  }
  public static @lombok.experimental.SuperBuilder class GrandChild extends Child {
    public static abstract @java.lang.SuppressWarnings("all") class GrandChildBuilder<C extends SuperBuilderAbstract.GrandChild, B extends SuperBuilderAbstract.GrandChild.GrandChildBuilder<C, B>> extends Child.ChildBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") String grandChildField;
      public GrandChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B grandChildField(final String grandChildField) {
        this.grandChildField = grandChildField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstract.GrandChild.GrandChildBuilder(super=" + super.toString()) + ", grandChildField=") + this.grandChildField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class GrandChildBuilderImpl extends SuperBuilderAbstract.GrandChild.GrandChildBuilder<SuperBuilderAbstract.GrandChild, SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl> {
      private GrandChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderAbstract.GrandChild build() {
        return new SuperBuilderAbstract.GrandChild(this);
      }
    }
    String grandChildField;
    protected @java.lang.SuppressWarnings("all") GrandChild(final SuperBuilderAbstract.GrandChild.GrandChildBuilder<?, ?> b) {
      super(b);
      this.grandChildField = b.grandChildField;
    }
    public static @java.lang.SuppressWarnings("all") SuperBuilderAbstract.GrandChild.GrandChildBuilder<?, ?> builder() {
      return new SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl();
    }
  }
  public SuperBuilderAbstract() {
    super();
  }
  public static void test() {
    GrandChild x = GrandChild.builder().grandChildField("").parentField(5).childField(2.5).build();
  }
}
