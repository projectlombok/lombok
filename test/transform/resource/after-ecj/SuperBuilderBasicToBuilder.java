import java.util.List;
public class SuperBuilderBasicToBuilder {
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int field1;
      private @java.lang.SuppressWarnings("all") int obtainViaField;
      private @java.lang.SuppressWarnings("all") int obtainViaMethod;
      private @java.lang.SuppressWarnings("all") String obtainViaStaticMethod;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") void $fillValuesFromInstanceIntoBuilder(final Parent instance, final ParentBuilder<?, ?> b) {
        b.field1(instance.field1);
        b.obtainViaField(instance.field1);
        b.obtainViaMethod(instance.method());
        b.obtainViaStaticMethod(Parent.staticMethod(instance));
        b.items(((instance.items == null) ? java.util.Collections.emptyList() : instance.items));
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field1(final int field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B obtainViaField(final int obtainViaField) {
        this.obtainViaField = obtainViaField;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B obtainViaMethod(final int obtainViaMethod) {
        this.obtainViaMethod = obtainViaMethod;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B obtainViaStaticMethod(final String obtainViaStaticMethod) {
        this.obtainViaStaticMethod = obtainViaStaticMethod;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B item(final String item) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.add(item);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B items(final java.util.Collection<? extends String> items) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.addAll(items);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B clearItems() {
        if ((this.items != null))
            this.items.clear();
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((((((("SuperBuilderBasicToBuilder.Parent.ParentBuilder(field1=" + this.field1) + ", obtainViaField=") + this.obtainViaField) + ", obtainViaMethod=") + this.obtainViaMethod) + ", obtainViaStaticMethod=") + this.obtainViaStaticMethod) + ", items=") + this.items) + ")");
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
    protected @java.lang.SuppressWarnings("all") Parent(final ParentBuilder<?, ?> b) {
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
    public @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> toBuilder() {
      return new ParentBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") ParentBuilder<?, ?> builder() {
      return new ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") double field3;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") void $fillValuesFromInstanceIntoBuilder(final Child instance, final ChildBuilder<?, ?> b) {
        b.field3(instance.field3);
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      public @java.lang.SuppressWarnings("all") B field3(final double field3) {
        this.field3 = field3;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderBasicToBuilder.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
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
    private double field3;
    protected @java.lang.SuppressWarnings("all") Child(final ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> toBuilder() {
      return new ChildBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") ChildBuilder<?, ?> builder() {
      return new ChildBuilderImpl();
    }
  }
  public SuperBuilderBasicToBuilder() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field3(0.0).field1(5).item("").build().toBuilder().build();
  }
}
