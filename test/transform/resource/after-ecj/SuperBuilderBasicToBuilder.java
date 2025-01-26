import java.util.List;
public class SuperBuilderBasicToBuilder {
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends SuperBuilderBasicToBuilder.Parent, B extends SuperBuilderBasicToBuilder.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int obtainViaField;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int obtainViaMethod;
      private @java.lang.SuppressWarnings("all") @lombok.Generated String obtainViaStaticMethod;
      private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        SuperBuilderBasicToBuilder.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderBasicToBuilder.Parent instance, final SuperBuilderBasicToBuilder.Parent.ParentBuilder<?, ?> b) {
        b.field1(instance.field1);
        b.obtainViaField(instance.field1);
        b.obtainViaMethod(instance.method());
        b.obtainViaStaticMethod(SuperBuilderBasicToBuilder.Parent.staticMethod(instance));
        b.items(((instance.items == null) ? java.util.Collections.<String>emptyList() : instance.items));
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B obtainViaField(final int obtainViaField) {
        this.obtainViaField = obtainViaField;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B obtainViaMethod(final int obtainViaMethod) {
        this.obtainViaMethod = obtainViaMethod;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B obtainViaStaticMethod(final String obtainViaStaticMethod) {
        this.obtainViaStaticMethod = obtainViaStaticMethod;
        return self();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated B item(final String item) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.add(item);
        return self();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated B items(final java.util.Collection<? extends String> items) {
        if ((items == null))
            {
              throw new java.lang.NullPointerException("items cannot be null");
            }
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.addAll(items);
        return self();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated B clearItems() {
        if ((this.items != null))
            this.items.clear();
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((((((((("SuperBuilderBasicToBuilder.Parent.ParentBuilder(field1=" + this.field1) + ", obtainViaField=") + this.obtainViaField) + ", obtainViaMethod=") + this.obtainViaMethod) + ", obtainViaStaticMethod=") + this.obtainViaStaticMethod) + ", items=") + this.items) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderBasicToBuilder.Parent.ParentBuilder<SuperBuilderBasicToBuilder.Parent, SuperBuilderBasicToBuilder.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Parent build() {
        return new SuperBuilderBasicToBuilder.Parent(this);
      }
    }
    private int field1;
    @lombok.Builder.ObtainVia(field = "field1") int obtainViaField;
    @lombok.Builder.ObtainVia(method = "method") int obtainViaMethod;
    @lombok.Builder.ObtainVia(method = "staticMethod",isStatic = true) String obtainViaStaticMethod;
    @lombok.Singular List<String> items;
    private int method() {
      return 2;
    }
    private static String staticMethod(Parent instance) {
      return "staticMethod";
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderBasicToBuilder.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
      this.obtainViaField = b.obtainViaField;
      this.obtainViaMethod = b.obtainViaMethod;
      this.obtainViaStaticMethod = b.obtainViaStaticMethod;
      java.util.List<String> items;
      switch (((b.items == null) ? 0 : b.items.size())) {
      case 0 :
          items = java.util.Collections.emptyList();
          break;
      case 1 :
          items = java.util.Collections.singletonList(b.items.get(0));
          break;
      default :
          items = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.items));
      }
      this.items = items;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Parent.ParentBuilder<?, ?> toBuilder() {
      return new SuperBuilderBasicToBuilder.Parent.ParentBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderBasicToBuilder.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderBasicToBuilder.Child, B extends SuperBuilderBasicToBuilder.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double field3;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        SuperBuilderBasicToBuilder.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderBasicToBuilder.Child instance, final SuperBuilderBasicToBuilder.Child.ChildBuilder<?, ?> b) {
        b.field3(instance.field3);
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
        return (((("SuperBuilderBasicToBuilder.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderBasicToBuilder.Child.ChildBuilder<SuperBuilderBasicToBuilder.Child, SuperBuilderBasicToBuilder.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Child build() {
        return new SuperBuilderBasicToBuilder.Child(this);
      }
    }
    private double field3;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderBasicToBuilder.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Child.ChildBuilder<?, ?> toBuilder() {
      return new SuperBuilderBasicToBuilder.Child.ChildBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderBasicToBuilder.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderBasicToBuilder.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderBasicToBuilder() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field3(0.0).field1(5).item("").build().toBuilder().build();
  }
}
