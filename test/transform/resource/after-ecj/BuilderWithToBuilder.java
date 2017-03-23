import java.util.List;
import lombok.Builder;
@Builder(toBuilder = true) @lombok.experimental.Accessors(prefix = "m") class BuilderWithToBuilder<T> {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderWithToBuilderBuilder<T> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String one;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String two;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") T foo;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.util.ArrayList<T> bars;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> one(final String one) {
      this.one = one;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> two(final String two) {
      this.two = two;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> foo(final T foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> bar(T bar) {
      if ((this.bars == null))
          this.bars = new java.util.ArrayList<T>();
      this.bars.add(bar);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> bars(java.util.Collection<? extends T> bars) {
      if ((this.bars == null))
          this.bars = new java.util.ArrayList<T>();
      this.bars.addAll(bars);
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> clearBars() {
      if ((this.bars != null))
          this.bars.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilder<T> build() {
      java.util.List<T> bars;
      switch (((this.bars == null) ? 0 : this.bars.size())) {
      case 0 :
          bars = java.util.Collections.emptyList();
          break;
      case 1 :
          bars = java.util.Collections.singletonList(this.bars.get(0));
          break;
      default :
          bars = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.bars));
      }
      return new BuilderWithToBuilder<T>(one, two, foo, bars);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((((((("BuilderWithToBuilder.BuilderWithToBuilderBuilder(one=" + this.one) + ", two=") + this.two) + ", foo=") + this.foo) + ", bars=") + this.bars) + ")");
    }
  }
  private String mOne;
  private String mTwo;
  private @Builder.ObtainVia(method = "rrr",isStatic = true) T foo;
  private @lombok.Singular List<T> bars;
  public static <K>K rrr(BuilderWithToBuilder<K> x) {
    return x.foo;
  }
  @java.beans.ConstructorProperties({"one", "two", "foo", "bars"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilder(final String one, final String two, final T foo, final List<T> bars) {
    super();
    this.mOne = one;
    this.mTwo = two;
    this.foo = foo;
    this.bars = bars;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T>BuilderWithToBuilderBuilder<T> builder() {
    return new BuilderWithToBuilderBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderWithToBuilderBuilder<T> toBuilder() {
    return new BuilderWithToBuilderBuilder<T>().one(this.mOne).two(this.mTwo).foo(BuilderWithToBuilder.rrr(this)).bars(this.bars);
  }
}
@lombok.experimental.Accessors(prefix = "m") class ConstructorWithToBuilder<T> {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class ConstructorWithToBuilderBuilder<T> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String mOne;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") T bar;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorWithToBuilderBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorWithToBuilderBuilder<T> mOne(final String mOne) {
      this.mOne = mOne;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorWithToBuilderBuilder<T> bar(final T bar) {
      this.bar = bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorWithToBuilder<T> build() {
      return new ConstructorWithToBuilder<T>(mOne, bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((("ConstructorWithToBuilder.ConstructorWithToBuilderBuilder(mOne=" + this.mOne) + ", bar=") + this.bar) + ")");
    }
  }
  private String mOne;
  private String mTwo;
  private T foo;
  private @lombok.Singular List<T> bars;
  public @Builder(toBuilder = true) ConstructorWithToBuilder(String mOne, @Builder.ObtainVia(field = "foo") T bar) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T>ConstructorWithToBuilderBuilder<T> builder() {
    return new ConstructorWithToBuilderBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") ConstructorWithToBuilderBuilder<T> toBuilder() {
    return new ConstructorWithToBuilderBuilder<T>().mOne(this.mOne).bar(this.foo);
  }
}
@lombok.experimental.Accessors(prefix = "m") class StaticWithToBuilder<T, K> {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class StaticWithToBuilderBuilder<Z> {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") String mOne;
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") Z bar;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") StaticWithToBuilderBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") StaticWithToBuilderBuilder<Z> mOne(final String mOne) {
      this.mOne = mOne;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") StaticWithToBuilderBuilder<Z> bar(final Z bar) {
      this.bar = bar;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") StaticWithToBuilder<Z, String> build() {
      return StaticWithToBuilder.<Z>test(mOne, bar);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (((("StaticWithToBuilder.StaticWithToBuilderBuilder(mOne=" + this.mOne) + ", bar=") + this.bar) + ")");
    }
  }
  private String mOne;
  private String mTwo;
  private T foo;
  private K bar;
  private @lombok.Singular List<T> bars;
  StaticWithToBuilder() {
    super();
  }
  public static @Builder(toBuilder = true) <Z>StaticWithToBuilder<Z, String> test(String mOne, @Builder.ObtainVia(field = "foo") Z bar) {
    return new StaticWithToBuilder<Z, String>();
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <Z>StaticWithToBuilderBuilder<Z> builder() {
    return new StaticWithToBuilderBuilder<Z>();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") StaticWithToBuilderBuilder<T> toBuilder() {
    return new StaticWithToBuilderBuilder<T>().mOne(this.mOne).bar(this.foo);
  }
}
