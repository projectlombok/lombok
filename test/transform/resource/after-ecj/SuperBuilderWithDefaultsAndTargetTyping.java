import java.util.Arrays;
import lombok.Builder;
public class SuperBuilderWithDefaultsAndTargetTyping {
  public static @lombok.experimental.SuperBuilder class Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilder<C extends SuperBuilderWithDefaultsAndTargetTyping.Parent, B extends SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilder<C, B>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String foo$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean foo$set;
      public ParentBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B foo(final String foo) {
        this.foo$value = foo;
        foo$set = true;
        return self();
      }
      protected abstract @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (("SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilder(foo$value=" + this.foo$value) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ParentBuilderImpl extends SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilder<SuperBuilderWithDefaultsAndTargetTyping.Parent, SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilderImpl> {
      private ParentBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Parent build() {
        return new SuperBuilderWithDefaultsAndTargetTyping.Parent(this);
      }
    }
    private @lombok.Builder.Default String foo;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated String $default$foo() {
      return doSth(Arrays.asList(1), Arrays.asList('a'));
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Parent(final SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilder<?, ?> b) {
      super();
      if (b.foo$set)
          this.foo = b.foo$value;
      else
          this.foo = SuperBuilderWithDefaultsAndTargetTyping.Parent.$default$foo();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilder<?, ?> builder() {
      return new SuperBuilderWithDefaultsAndTargetTyping.Parent.ParentBuilderImpl();
    }
  }
  public static @lombok.experimental.SuperBuilder class Child extends Parent {
    public static abstract @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilder<C extends SuperBuilderWithDefaultsAndTargetTyping.Child, B extends SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated String foo$value;
      private @java.lang.SuppressWarnings("all") @lombok.Generated boolean foo$set;
      public ChildBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated B foo(final String foo) {
        this.foo$value = foo;
        foo$set = true;
        return self();
      }
      protected abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated B self();
      public abstract @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated C build();
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilder(super=" + super.toString()) + ", foo$value=") + this.foo$value) + ")");
      }
    }
    private static final @java.lang.SuppressWarnings("all") @lombok.Generated class ChildBuilderImpl extends SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilder<SuperBuilderWithDefaultsAndTargetTyping.Child, SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilderImpl> {
      private ChildBuilderImpl() {
        super();
      }
      protected @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilderImpl self() {
        return this;
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Child build() {
        return new SuperBuilderWithDefaultsAndTargetTyping.Child(this);
      }
    }
    private @lombok.Builder.Default String foo;
    private static @java.lang.SuppressWarnings("all") @lombok.Generated String $default$foo() {
      return doSth(Arrays.asList(1), Arrays.asList('a'));
    }
    protected @java.lang.SuppressWarnings("all") @lombok.Generated Child(final SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilder<?, ?> b) {
      super(b);
      if (b.foo$set)
          this.foo = b.foo$value;
      else
          this.foo = SuperBuilderWithDefaultsAndTargetTyping.Child.$default$foo();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilder<?, ?> builder() {
      return new SuperBuilderWithDefaultsAndTargetTyping.Child.ChildBuilderImpl();
    }
  }
  public SuperBuilderWithDefaultsAndTargetTyping() {
    super();
  }
  static String doSth(java.util.List<Integer> i, java.util.List<Character> c) {
    return null;
  }
}
