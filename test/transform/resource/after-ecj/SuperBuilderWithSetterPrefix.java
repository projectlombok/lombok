import java.util.List;
public class SuperBuilderWithSetterPrefix {
  public static @lombok.experimental.SuperBuilder(toBuilder = true,setterPrefix = "with") class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends SuperBuilderWithSetterPrefix.Parent, B extends SuperBuilderWithSetterPrefix.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int obtainViaField;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int obtainViaMethod;
      private @java.lang.SuppressWarnings("all") @lombok.Generated String obtainViaStaticMethod;
      private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        SuperBuilderWithSetterPrefix.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithSetterPrefix.Parent instance, final SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
        b.withField1(instance.field1);
        b.withObtainViaField(instance.field1);
        b.withObtainViaMethod(instance.method());
        b.withObtainViaStaticMethod(SuperBuilderWithSetterPrefix.Parent.staticMethod(instance));
        b.withItems(((instance.items == null) ? java.util.Collections.<String>emptyList() : instance.items));
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withField1(final int field1) {
        this.field1 = field1;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withObtainViaField(final int obtainViaField) {
        this.obtainViaField = obtainViaField;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withObtainViaMethod(final int obtainViaMethod) {
        this.obtainViaMethod = obtainViaMethod;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withObtainViaStaticMethod(final String obtainViaStaticMethod) {
        this.obtainViaStaticMethod = obtainViaStaticMethod;
        return self();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withItem(final String item) {
        if ((this.items == null))
            this.items = new java.util.ArrayList<String>();
        this.items.add(item);
        return self();
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated B withItems(final java.util.Collection<? extends String> items) {
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
        return (((((((((("SuperBuilderWithSetterPrefix.Parent.ParentBuilder(field1=" + this.field1) + ", obtainViaField=") + this.obtainViaField) + ", obtainViaMethod=") + this.obtainViaMethod) + ", obtainViaStaticMethod=") + this.obtainViaStaticMethod) + ", items=") + this.items) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderWithSetterPrefix.Parent.ParentBuilder<SuperBuilderWithSetterPrefix.Parent, SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Parent build() {
        return new SuperBuilderWithSetterPrefix.Parent(this);
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
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
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
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> toBuilder() {
      return new SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true,setterPrefix = "set") class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithSetterPrefix.Child, B extends SuperBuilderWithSetterPrefix.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double field3;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        SuperBuilderWithSetterPrefix.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") @lombok.Generated void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithSetterPrefix.Child instance, final SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> b) {
        b.setField3(instance.field3);
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B setField3(final double field3) {
        this.field3 = field3;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithSetterPrefix.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithSetterPrefix.Child.ChildBuilder<SuperBuilderWithSetterPrefix.Child, SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Child build() {
        return new SuperBuilderWithSetterPrefix.Child(this);
      }
    }
    private double field3;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> toBuilder() {
      return new SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithSetterPrefix() {
    super();
  }
  public static void test() {
    Child x = Child.builder().setField3(0.0).withField1(5).withItem("").build().toBuilder().build();
  }
}
