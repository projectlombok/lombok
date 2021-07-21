import java.util.List;
import lombok.Singular;
class CheckerFrameworkSuperBuilder {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ParentBuilder<C extends CheckerFrameworkSuperBuilder.Parent, B extends CheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") int x$value;
      private @java.lang.SuppressWarnings("all") boolean x$set;
      private @java.lang.SuppressWarnings("all") int y;
      private @java.lang.SuppressWarnings("all") int z;
      private @java.lang.SuppressWarnings("all") java.util.ArrayList<String> names;
      public ParentBuilder() {
        super();
      }
      protected abstract @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B self();
      public abstract @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(CheckerFrameworkSuperBuilder.Parent. @org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilder<C, B> this);
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B x(final int x) {
        this.x$value = x;
        x$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B y(final int y) {
        this.y = y;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B z(final int z) {
        this.z = z;
        return self();
      }
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B name(final String name) {
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.add(name);
        return self();
      }
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B names(final java.util.Collection<? extends String> names) {
        if ((names == null))
            {
              throw new java.lang.NullPointerException("names cannot be null");
            }
        if ((this.names == null))
            this.names = new java.util.ArrayList<String>();
        this.names.addAll(names);
        return self();
      }
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B clearNames() {
        if ((this.names != null))
            this.names.clear();
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((((("CheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value) + ", y=") + this.y) + ", z=") + this.z) + ", names=") + this.names) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ParentBuilderImpl extends CheckerFrameworkSuperBuilder.Parent.ParentBuilder<CheckerFrameworkSuperBuilder.Parent, CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.common.returnsreceiver.qual.This ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.Parent build(CheckerFrameworkSuperBuilder.Parent.@org.checkerframework.checker.calledmethods.qual.CalledMethods({"y", "z"}) ParentBuilderImpl this) {
        return new CheckerFrameworkSuperBuilder.Parent(this);
      }
    }
    @lombok.Builder.Default int x;
    int y;
    int z;
    @Singular List<String> names;
    private static @java.lang.SuppressWarnings("all") int $default$x() {
      return 5;
    }
    protected @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") Parent(final CheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> b) {
      super();
      if (b.x$set)
          this.x = b.x$value;
      else
          this.x = CheckerFrameworkSuperBuilder.Parent.$default$x();
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
    public static @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.Parent. @org.checkerframework.common.aliasing.qual.Unique ParentBuilder<?, ?> builder() {
      return new CheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class ZChild extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") class ZChildBuilder<C extends CheckerFrameworkSuperBuilder.ZChild, B extends CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") int a$value;
      private @java.lang.SuppressWarnings("all") boolean a$set;
      private @java.lang.SuppressWarnings("all") int b;
      public ZChildBuilder() {
        super();
      }
      protected abstract @java.lang.Override @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B self();
      public abstract @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") C build(CheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilder<C, B> this);
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B a(final int a) {
        this.a$value = a;
        a$set = true;
        return self();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @org.checkerframework.common.returnsreceiver.qual.This B b(final int b) {
        this.b = b;
        return self();
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") java.lang.String toString() {
        return (((((("CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder(super=" + super.toString()) + ", a$value=") + this.a$value) + ", b=") + this.b) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") class ZChildBuilderImpl extends CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<CheckerFrameworkSuperBuilder.ZChild, CheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl> {
      private ZChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @org.checkerframework.dataflow.qual.Pure @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.common.returnsreceiver.qual.This ZChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.ZChild build(CheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.checker.calledmethods.qual.CalledMethods("b") ZChildBuilderImpl this) {
        return new CheckerFrameworkSuperBuilder.ZChild(this);
      }
    }
    @lombok.Builder.Default int a;
    int b;
    private static @java.lang.SuppressWarnings("all") int $default$a() {
      return 1;
    }
    protected @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") ZChild(final CheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<?, ?> b) {
      super(b);
      if (b.a$set)
          this.a = b.a$value;
      else
          this.a = CheckerFrameworkSuperBuilder.ZChild.$default$a();
      this.b = b.b;
    }
    public static @org.checkerframework.dataflow.qual.SideEffectFree @java.lang.SuppressWarnings("all") CheckerFrameworkSuperBuilder.ZChild. @org.checkerframework.common.aliasing.qual.Unique ZChildBuilder<?, ?> builder() {
      return new CheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl();
    }
  }
  CheckerFrameworkSuperBuilder() {
    super();
  }
}
