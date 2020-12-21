import java.util.Map;
public class SuperBuilderWithGenericsAndToBuilder {
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<A, C extends SuperBuilderWithGenericsAndToBuilder.Parent<A>, B extends SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, C, B>> {
      private @java.lang.SuppressWarnings("all") A field1;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<Integer> items$key;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> items$value;
      public ParentBuilder() {
        super();
      }
      protected @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") <A>void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithGenericsAndToBuilder.Parent<A> instance, final SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> b) {
        b.field1(instance.field1);
        b.items(((instance.items == null) ? java.util.Collections.emptyMap() : instance.items));
      }
      protected abstract @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B field1(final A field1) {
        this.field1 = field1;
        return self();
      }
      public @java.lang.SuppressWarnings("all") B item(final Integer itemKey, final String itemValue) {
        if ((this.items$key == null))
            {
              this.items$key = new java.util.ArrayList<Integer>();
              this.items$value = new java.util.ArrayList<String>();
            }
        this.items$key.add(itemKey);
        this.items$value.add(itemValue);
        return self();
      }
      public @java.lang.SuppressWarnings("all") B items(final java.util.Map<? extends Integer, ? extends String> items) {
        if ((items == null))
            {
              throw new java.lang.NullPointerException("items cannot be null");
            }
        if ((this.items$key == null))
            {
              this.items$key = new java.util.ArrayList<Integer>();
              this.items$value = new java.util.ArrayList<String>();
            }
        for (java.util.Map.Entry<? extends Integer, ? extends String> $lombokEntry : items.entrySet()) 
          {
            this.items$key.add($lombokEntry.getKey());
            this.items$value.add($lombokEntry.getValue());
          }
        return self();
      }
      public @java.lang.SuppressWarnings("all") B clearItems() {
        if ((this.items$key != null))
            {
              this.items$key.clear();
              this.items$value.clear();
            }
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((("SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder(field1=" + this.field1) + ", items$key=") + this.items$key) + ", items$value=") + this.items$value) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl<A> extends SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, SuperBuilderWithGenericsAndToBuilder.Parent<A>, SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Parent<A> build() {
        return new SuperBuilderWithGenericsAndToBuilder.Parent<A>(this);
      }
    }
    A field1;
    @lombok.Singular Map<Integer, String> items;
    protected @java.lang.SuppressWarnings("all") Parent(final SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> b) {
      super();
      this.field1 = b.field1;
      java.util.Map<Integer, String> items;
      switch (((b.items$key == null) ? 0 : b.items$key.size())) {
      case 0 : 
          items = java.util.Collections.emptyMap();
          break;
      case 1 : 
          items = java.util.Collections.singletonMap(b.items$key.get(0), b.items$value.get(0));
          break;
      default : 
          items = new java.util.LinkedHashMap<Integer, String>(((b.items$key.size() < 0x40000000) ? ((1 + b.items$key.size()) + ((b.items$key.size() - 3) / 3)) : java.lang.Integer.MAX_VALUE));
          for (int $i = 0;; ($i < b.items$key.size()); $i ++) 
            items.put(b.items$key.get($i), b.items$value.get($i));
          items = java.util.Collections.unmodifiableMap(items);
      }
      this.items = items;
    }
    public @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> toBuilder() {
      return new SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") <A>SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> builder() {
      return new SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>();
    }
  }
  public static @lombok.experimental.SuperBuilder(toBuilder = true) class Child<A> extends Parent<A> {
    public static abstract @java.lang.SuppressWarnings("all") class ChildBuilder<A, C extends SuperBuilderWithGenericsAndToBuilder.Child<A>, B extends SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, C, B>> extends Parent.ParentBuilder<A, C, B> {
      private @java.lang.SuppressWarnings("all") double field3;
      public ChildBuilder() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") B $fillValuesFrom(final C instance) {
        super.$fillValuesFrom(instance);
        SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
        return self();
      }
      private static @java.lang.SuppressWarnings("all") <A>void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithGenericsAndToBuilder.Child<A> instance, final SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> b) {
        b.field3(instance.field3);
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") C build();
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") B field3(final double field3) {
        this.field3 = field3;
        return self();
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((("SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ChildBuilderImpl<A> extends SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, SuperBuilderWithGenericsAndToBuilder.Child<A>, SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A> self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Child<A> build() {
        return new SuperBuilderWithGenericsAndToBuilder.Child<A>(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") Child(final SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public @java.lang.SuppressWarnings("all") SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> toBuilder() {
      return new SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>().$fillValuesFrom(this);
    }
    public static @java.lang.SuppressWarnings("all") <A>SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> builder() {
      return new SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>();
    }
  }
  public SuperBuilderWithGenericsAndToBuilder() {
    super();
  }
  public static void test() {
    Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item(5, "").build().toBuilder().build();
  }
}
