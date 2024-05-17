import java.util.Set;
import lombok.Builder;
import lombok.Value;
public class BuilderWithRecursiveGenerics {
  interface Inter<T, U extends Inter<T, U>> {
  }
  public static final @Builder @Value class Test<Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> {
    public static @java.lang.SuppressWarnings("all") @lombok.Generated class TestBuilder<Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> {
      private @java.lang.SuppressWarnings("all") @lombok.Generated Foo foo;
      private @java.lang.SuppressWarnings("all") @lombok.Generated Bar bar;
      @java.lang.SuppressWarnings("all") @lombok.Generated TestBuilder() {
        super();
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithRecursiveGenerics.Test.TestBuilder<Foo, Bar, Quz> foo(final Foo foo) {
        this.foo = foo;
        return this;
      }
      /**
       * @return {@code this}.
       */
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithRecursiveGenerics.Test.TestBuilder<Foo, Bar, Quz> bar(final Bar bar) {
        this.bar = bar;
        return this;
      }
      public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithRecursiveGenerics.Test<Foo, Bar, Quz> build() {
        return new BuilderWithRecursiveGenerics.Test<Foo, Bar, Quz>(this.foo, this.bar);
      }
      public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
        return (((("BuilderWithRecursiveGenerics.Test.TestBuilder(foo=" + this.foo) + ", bar=") + this.bar) + ")");
      }
    }
    private final Foo foo;
    private final Bar bar;
    @java.lang.SuppressWarnings("all") @lombok.Generated Test(final Foo foo, final Bar bar) {
      super();
      this.foo = foo;
      this.bar = bar;
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated <Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>>BuilderWithRecursiveGenerics.Test.TestBuilder<Foo, Bar, Quz> builder() {
      return new BuilderWithRecursiveGenerics.Test.TestBuilder<Foo, Bar, Quz>();
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Foo getFoo() {
      return this.foo;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated Bar getBar() {
      return this.bar;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated boolean equals(final java.lang.Object o) {
      if ((o == this))
          return true;
      if ((! (o instanceof BuilderWithRecursiveGenerics.Test)))
          return false;
      final BuilderWithRecursiveGenerics.Test<?, ?, ?> other = (BuilderWithRecursiveGenerics.Test<?, ?, ?>) o;
      final java.lang.Object this$foo = this.getFoo();
      final java.lang.Object other$foo = other.getFoo();
      if (((this$foo == null) ? (other$foo != null) : (! this$foo.equals(other$foo))))
          return false;
      final java.lang.Object this$bar = this.getBar();
      final java.lang.Object other$bar = other.getBar();
      if (((this$bar == null) ? (other$bar != null) : (! this$bar.equals(other$bar))))
          return false;
      return true;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final java.lang.Object $foo = this.getFoo();
      result = ((result * PRIME) + (($foo == null) ? 43 : $foo.hashCode()));
      final java.lang.Object $bar = this.getBar();
      result = ((result * PRIME) + (($bar == null) ? 43 : $bar.hashCode()));
      return result;
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (((("BuilderWithRecursiveGenerics.Test(foo=" + this.getFoo()) + ", bar=") + this.getBar()) + ")");
    }
  }
  public BuilderWithRecursiveGenerics() {
    super();
  }
}
