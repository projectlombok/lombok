public class SuperBuilderAbstract {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int parentField;
      public ParentBuilder() {
        super();
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B parentField(final int parentField) {
        this.parentField = parentField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderAbstract.Parent.ParentBuilder(parentField=" + this.parentField) + ")");
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
    int parentField;
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<?, ?> b) {
      super();
      this.parentField = b.parentField;
    }
    public static @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
      return new ParentBuilderImpl();
    }
  }
  public static abstract @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double childField;
      public ChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B childField(final double childField) {
        this.childField = childField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstract.Child.ChildBuilder(super=" + super.toString()) + ", childField=") + this.childField) + ")");
      }
    }
    double childField;
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.childField = b.childField;
    }
  }
  public static @lombok.experimental.SuperBuilder class GrandChild extends Child {
    public static abstract @java.lang.SuppressWarnings("all") class GrandChildBuilder<C extends GrandChild, B extends GrandChildBuilder<C, B>> extends Child.ChildBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") String grandChildField;
      public GrandChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B grandChildField(final String grandChildField) {
        this.grandChildField = grandChildField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstract.GrandChild.GrandChildBuilder(super=" + super.toString()) + ", grandChildField=") + this.grandChildField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class GrandChildBuilderImpl extends GrandChildBuilder<GrandChild, GrandChildBuilderImpl> {
      private GrandChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") GrandChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") GrandChild build() {
        return new GrandChild(this);
      }
    }
    String grandChildField;
    protected @java.lang.SuppressWarnings("all") GrandChild(final GrandChildBuilder<?, ?> b) {
      super(b);
      this.grandChildField = b.grandChildField;
    }
    public static @java.lang.SuppressWarnings("all") GrandChildBuilder<?, ?> builder() {
      return new GrandChildBuilderImpl();
    }
  }
  public SuperBuilderAbstract() {
    super();
  }
  public static void test() {
    GrandChild x = GrandChild.builder().grandChildField("").parentField(5).childField(2.5).build();
  }
}
