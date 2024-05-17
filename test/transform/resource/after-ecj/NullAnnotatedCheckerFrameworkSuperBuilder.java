import java.util.List;
import lombok.Singular;
class NullAnnotatedCheckerFrameworkSuperBuilder {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent, B extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int x$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean x$set;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int y;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int z;
      private @java.lang.SuppressWarnings("all") @lombok.Generated java.util.ArrayList<String> names;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B x(final int x) {
        this.x$value = x;
        x$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B y(final int y) {
        this.y = y;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B z(final int z) {
        this.z = z;
        return self();
      }
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B name(final String name) {
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.add(name);
        return self();
      }
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B names(final java.util. @org.checkerframework.checker.nullness.qual.NonNull Collection<? extends String> names) {
        if ((names == null))
            {
              throw new java.lang.NullPointerException("names cannot be null");
            }
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.addAll(names);
        return self();
      }
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B clearNames() {
        if ((this.names != null))
            this.names.clear();
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
        return (((((((("NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ", z=") + this.z) + ", names=") + this.names) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.Parent, NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.@org.checkerframework.checker.nullness.qual.NonNull Parent build() {
        return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent(this);
      }
    }
    @lombok.Builder.Default int x;
    int y;
    int z;
    @Singular List<String> names;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated int $default$x() {
      return 5;
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> b) {
      super();
      if (b.x$set)
          this.x = b.x$value;
      else
          this.x = NullAnnotatedCheckerFrameworkSuperBuilder.Parent.$default$x();
      this.y = b.y;
      this.z = b.z;
      java.util.List<String> names;
      switch (((b.names == null) ? 0 : b.names.size())) {
      case 0 :
          names = java.util.Collections.emptyList();
          break;
      case 1 :
          names = java.util.Collections.singletonList(b.names.get(0));
          break;
      default :
          names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.names));
      }
      this.names = names;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.nullness.qual.NonNull ParentBuilder<?, ?> builder() {
      return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class ZChild extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ZChildBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, B extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated int a$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean a$set;
      private @java.lang.SuppressWarnings("all") @lombok.Generated int b;
      public ZChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B a(final int a) {
        this.a$value = a;
        a$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated B b(final int b) {
        this.b = b;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
        return (((((("NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder(super=" + super.toString()) + ", a$value=") + this.a$value) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ZChildBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl> {
      private ZChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.@org.checkerframework.checker.nullness.qual.NonNull ZChild build() {
        return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild(this);
      }
    }
    @lombok.Builder.Default int a;
    int b;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated int $default$a() {
      return 1;
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated ZChild(final NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<?, ?> b) {
      super(b);
      if (b.a$set)
          this.a = b.a$value;
      else
          this.a = NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.$default$a();
      this.b = b.b;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated NullAnnotatedCheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.checker.nullness.qual.NonNull ZChildBuilder<?, ?> builder() {
      return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl();
    }
  }
  NullAnnotatedCheckerFrameworkSuperBuilder() {
    super();
  }
}
