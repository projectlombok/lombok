import java.util.List;
public class SuperBuilderExperimental {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends SuperBuilderExperimental.Parent, B extends SuperBuilderExperimental.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int field1;
      private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> items;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B field1(final int field1) {
        this.field1 = field1;
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
        return (((("SuperBuilderExperimental.Parent.ParentBuilder(field1=" + this.field1) + ", items=") + this.items) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderExperimental.Parent.ParentBuilder<SuperBuilderExperimental.Parent, SuperBuilderExperimental.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Parent build() {
        return new SuperBuilderExperimental.Parent(this);
      }
    }
    int field1;
    @lombok.Singular List<String> items;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderExperimental.Parent.ParentBuilder<?, ?> b) {
      super();
      this.field1 = b.field1;
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
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderExperimental.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends SuperBuilderExperimental.Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderExperimental.Child, B extends SuperBuilderExperimental.Child.ChildBuilder<C, B>> extends SuperBuilderExperimental.Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated double field3;
      public ChildBuilder() {
        super();
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
        return (((("SuperBuilderExperimental.Child.ChildBuilder(super=" + super.toString()) + ", field3=") + this.field3) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderExperimental.Child.ChildBuilder<SuperBuilderExperimental.Child, SuperBuilderExperimental.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Child build() {
        return new SuperBuilderExperimental.Child(this);
      }
    }
    double field3;
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderExperimental.Child.ChildBuilder<?, ?> b) {
      super(b);
      this.field3 = b.field3;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderExperimental.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderExperimental.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderExperimental() {
    super();
  }
  public static void test() {
    Child x = Child.builder().field3(0.0).field1(5).item("").build();
  }
}
