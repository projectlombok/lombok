public class SuperBuilderAbstractToBuilder {
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends SuperBuilderAbstractToBuilder.Parent, B extends SuperBuilderAbstractToBuilder.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int parentField;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        SuperBuilderAbstractToBuilder.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderAbstractToBuilder.Parent instance, final SuperBuilderAbstractToBuilder.Parent.ParentBuilder<?, ?> b) {
        b.parentField(instance.parentField);
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B parentField(final int parentField) {
        this.parentField = parentField;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderAbstractToBuilder.Parent.ParentBuilder(parentField=" + this.parentField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderAbstractToBuilder.Parent.ParentBuilder<SuperBuilderAbstractToBuilder.Parent, SuperBuilderAbstractToBuilder.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.Parent build() {
        return new SuperBuilderAbstractToBuilder.Parent(this);
      }
    }
    int parentField;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderAbstractToBuilder.Parent.ParentBuilder<?, ?> b) {
      super();
      this.parentField = b.parentField;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.Parent.ParentBuilder<?, ?> toBuilder() {
      return new SuperBuilderAbstractToBuilder.Parent.ParentBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderAbstractToBuilder.Parent.ParentBuilderImpl();
    }
  }
  public static abstract @lombok.experimental.SuperBuilder(toBuilder = true) class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderAbstractToBuilder.Child, B extends SuperBuilderAbstractToBuilder.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double childField;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        SuperBuilderAbstractToBuilder.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderAbstractToBuilder.Child instance, final SuperBuilderAbstractToBuilder.Child.ChildBuilder<?, ?> b) {
        b.childField(instance.childField);
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B childField(final double childField) {
        this.childField = childField;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderAbstractToBuilder.Child.ChildBuilder(super=" + super.toString()) + ", childField=") + this.childField) + ")");
      }
    }
    double childField;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderAbstractToBuilder.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.childField = b.childField;
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class GrandChild extends Child {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class GrandChildBuilder<C extends SuperBuilderAbstractToBuilder.GrandChild, B extends SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<C, B>> extends Child.ChildBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String grandChildField;
      public GrandChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderAbstractToBuilder.GrandChild instance, final SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<?, ?> b) {
        b.grandChildField(instance.grandChildField);
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B grandChildField(final String grandChildField) {
        this.grandChildField = grandChildField;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder(super=" + super.toString()) + ", grandChildField=") + this.grandChildField) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class GrandChildBuilderImpl extends SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<SuperBuilderAbstractToBuilder.GrandChild, SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilderImpl> {
      private GrandChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.GrandChild build() {
        return new SuperBuilderAbstractToBuilder.GrandChild(this);
      }
    }
    String grandChildField;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated GrandChild(final SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<?, ?> b) {
      super(b);
      this.grandChildField = b.grandChildField;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<?, ?> toBuilder() {
      return new SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilder<?, ?> builder() {
      return new SuperBuilderAbstractToBuilder.GrandChild.GrandChildBuilderImpl();
    }
  }
  public SuperBuilderAbstractToBuilder() {
    super();
  }
  public static void test() {
    GrandChild x = GrandChild.builder().grandChildField("").parentField(5).childField(2.5).build().toBuilder().build();
  }
}
