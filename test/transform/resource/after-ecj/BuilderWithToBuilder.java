import java.util.List;
import lombok.Builder;
@Builder(toBuilder = true) @lombok.experimental.Accessors(prefix = "m") class BuilderWithToBuilder<T> {
  public static @java.lang.SuppressWarnings("all") class BuilderWithToBuilderBuilder<T> {
    private @java.lang.SuppressWarnings("all") String one;
    private @java.lang.SuppressWarnings("all") String two;
    private @java.lang.SuppressWarnings("all") T foo;
    private @java.lang.SuppressWarnings("all") java.util.ArrayList<T> bars;
    @java.lang.SuppressWarnings("all") BuilderWithToBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> one(final String one) {
      this.one = one;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> two(final String two) {
      this.two = two;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> foo(final T foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> bar(final T bar) {
      if ((this.bars == null))
          this.bars = new java.util.ArrayList<T>();
      this.bars.add(bar);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> bars(final java.util.Collection<? extends T> bars) {
      if ((bars == null))
          {
            throw new java.lang.NullPointerException("bars cannot be null");
          }
      if ((this.bars == null))
          this.bars = new java.util.ArrayList<T>();
      this.bars.addAll(bars);
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> clearBars() {
      if ((this.bars != null))
          this.bars.clear();
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderWithToBuilder<T> build() {
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
      return new BuilderWithToBuilder<T>(this.one, this.two, this.foo, bars);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
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
  @java.lang.SuppressWarnings("all") BuilderWithToBuilder(final String one, final String two, final T foo, final List<T> bars) {
    super();
    this.mOne = one;
    this.mTwo = two;
    this.foo = foo;
    this.bars = bars;
  }
  public static @java.lang.SuppressWarnings("all") <T>BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> builder() {
    return new BuilderWithToBuilder.BuilderWithToBuilderBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> toBuilder() {
    final T foo = BuilderWithToBuilder.<T>rrr(this);
    final BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> builder = new BuilderWithToBuilder.BuilderWithToBuilderBuilder<T>().one(this.mOne).two(this.mTwo).foo(foo);
    if ((this.bars != null))
        builder.bars(this.bars);
    return builder;
  }
}
@lombok.experimental.Accessors(prefix = "m") class ConstructorWithToBuilder<T> {
  public static @java.lang.SuppressWarnings("all") class ConstructorWithToBuilderBuilder<T> {
    private @java.lang.SuppressWarnings("all") String mOne;
    private @java.lang.SuppressWarnings("all") T baz;
    private @java.lang.SuppressWarnings("all") com.google.common.collect.ImmutableList<T> bars;
    @java.lang.SuppressWarnings("all") ConstructorWithToBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> mOne(final String mOne) {
      this.mOne = mOne;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> baz(final T baz) {
      this.baz = baz;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> bars(final com.google.common.collect.ImmutableList<T> bars) {
      this.bars = bars;
      return this;
    }
    public @java.lang.SuppressWarnings("all") ConstructorWithToBuilder<T> build() {
      return new ConstructorWithToBuilder<T>(this.mOne, this.baz, this.bars);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("ConstructorWithToBuilder.ConstructorWithToBuilderBuilder(mOne=" + this.mOne) + ", baz=") + this.baz) + ", bars=") + this.bars) + ")");
    }
  }
  private String mOne;
  private String mTwo;
  private T foo;
  private @lombok.Singular com.google.common.collect.ImmutableList<T> bars;
  public @Builder(toBuilder = true) ConstructorWithToBuilder(String mOne, @Builder.ObtainVia(field = "foo") T baz, com.google.common.collect.ImmutableList<T> bars) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") <T>ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> builder() {
    return new ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> toBuilder() {
    return new ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T>().mOne(this.mOne).baz(this.foo).bars(this.bars);
  }
}
class StaticMethodWithToBuilder<T> {
  public static @java.lang.SuppressWarnings("all") class StaticMethodWithToBuilderBuilder<T> {
    private @java.lang.SuppressWarnings("all") T foo;
    @java.lang.SuppressWarnings("all") StaticMethodWithToBuilderBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> foo(final T foo) {
      this.foo = foo;
      return this;
    }
    public @java.lang.SuppressWarnings("all") StaticMethodWithToBuilder<T> build() {
      return StaticMethodWithToBuilder.<T>of(this.foo);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder(foo=" + this.foo) + ")");
    }
  }
  private T foo;
  public StaticMethodWithToBuilder(T foo) {
    super();
    this.foo = foo;
  }
  public static @Builder(toBuilder = true) <T>StaticMethodWithToBuilder<T> of(T foo) {
    return new StaticMethodWithToBuilder<T>(foo);
  }
  public static @java.lang.SuppressWarnings("all") <T>StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> builder() {
    return new StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T>();
  }
  public @java.lang.SuppressWarnings("all") StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> toBuilder() {
    return new StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T>().foo(this.foo);
  }
}