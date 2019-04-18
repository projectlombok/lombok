public class SuperBuilderAbstractToBuilder {
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int parentField;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") void $fillValuesFromInstanceIntoBuilder(final Parent instance, final ParentBuilder<?, ?> b) {
        b.parentField(instance.parentField);
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B parentField(final int parentField) {
        this.parentField = parentField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (("SuperBuilderAbstractToBuilder.Parent.ParentBuilder(parentField=" + this.parentField) + ")");
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
    public @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> toBuilder() {
      return new ParentBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
      return new ParentBuilderImpl();
    }
  }
  public static abstract @lombok.experimental.SuperBuilder(toBuilder = true) class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double childField;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") void $fillValuesFromInstanceIntoBuilder(final Child instance, final ChildBuilder<?, ?> b) {
        b.childField(instance.childField);
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B childField(final double childField) {
        this.childField = childField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstractToBuilder.Child.ChildBuilder(super=" + super.toString()) + ", childField=") + this.childField) + ")");
      }
    }
    double childField;
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.childField = b.childField;
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class GrandChild extends Child {
    public static abstract @java.lang.SuppressWarnings("all") class GrandChildBuilder<C extends GrandChild, B extends GrandChildBuilder<C, B>> extends Child.ChildBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") String grandChildField;
      public GrandChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        GrandChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") void $fillValuesFromInstanceIntoBuilder(final GrandChild instance, final GrandChildBuilder<?, ?> b) {
        b.grandChildField(instance.grandChildField);
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B grandChildField(final String grandChildField) {
        this.grandChildField = grandChildField;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder(super=" + super.toString()) + ", grandChildField=") + this.grandChildField) + ")");
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
    public @java.lang.SuppressWarnings("all") GrandChildBuilder<?, ?> toBuilder() {
      return new GrandChildBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") GrandChildBuilder<?, ?> builder() {
      return new GrandChildBuilderImpl();
    }
  }
  public SuperBuilderAbstractToBuilder() {
    super();
  }
  public static void test() {
    GrandChild x = GrandChild.builder().grandChildField("").parentField(5).childField(2.5).build().toBuilder().build();
  }
}
