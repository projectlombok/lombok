import lombok.Builder;
import java.util.*;
public @Builder class BuilderDefaultsGenerics<N extends Number, T, R extends List<T>> {
  public static @java.lang.SuppressWarnings("all") class BuilderDefaultsGenericsBuilder<N extends Number, T, R extends List<T>> {
    private @java.lang.SuppressWarnings("all") java.util.concurrent.Callable<N> callable$value;
    private @java.lang.SuppressWarnings("all") boolean callable$set;
    private @java.lang.SuppressWarnings("all") T tee$value;
    private @java.lang.SuppressWarnings("all") boolean tee$set;
    private @java.lang.SuppressWarnings("all") R arrr$value;
    private @java.lang.SuppressWarnings("all") boolean arrr$set;
    @java.lang.SuppressWarnings("all") BuilderDefaultsGenericsBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> callable(final java.util.concurrent.Callable<N> callable) {
      this.callable$value = callable;
      callable$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> tee(final T tee) {
      this.tee$value = tee;
      tee$set = true;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> arrr(final R arrr) {
      this.arrr$value = arrr;
      arrr$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics<N, T, R> build() {
      java.util.concurrent.Callable<N> callable$value = this.callable$value;
      if ((! this.callable$set))
          callable$value = BuilderDefaultsGenerics.<N, T, R>$default$callable();
      T tee$value = this.tee$value;
      if ((! this.tee$set))
          tee$value = BuilderDefaultsGenerics.<N, T, R>$default$tee();
      R arrr$value = this.arrr$value;
      if ((! this.arrr$set))
          arrr$value = BuilderDefaultsGenerics.<N, T, R>$default$arrr();
      return new BuilderDefaultsGenerics<N, T, R>(callable$value, tee$value, arrr$value);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder(callable$value=" + this.callable$value) + ", tee$value=") + this.tee$value) + ", arrr$value=") + this.arrr$value) + ")");
    }
  }
  private @Builder.Default java.util.concurrent.Callable<N> callable;
  private @Builder.Default T tee;
  private @Builder.Default R arrr;
  private static @java.lang.SuppressWarnings("all") <N extends Number, T, R extends List<T>>java.util.concurrent.Callable<N> $default$callable() {
    return null;
  }
  private static @java.lang.SuppressWarnings("all") <N extends Number, T, R extends List<T>>T $default$tee() {
    return null;
  }
  private static @java.lang.SuppressWarnings("all") <N extends Number, T, R extends List<T>>R $default$arrr() {
    return null;
  }
  @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics(final java.util.concurrent.Callable<N> callable, final T tee, final R arrr) {
    super();
    this.callable = callable;
    this.tee = tee;
    this.arrr = arrr;
  }
  public static @java.lang.SuppressWarnings("all") <N extends Number, T, R extends List<T>>BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> builder() {
    return new BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R>();
  }
}
