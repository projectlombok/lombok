import lombok.Builder;
import java.util.*;
public @Builder class BuilderDefaultsGenerics<N extends Number, T, R extends List<T>> {
  public static @java.lang.SuppressWarnings("all") class BuilderDefaultsGenericsBuilder<N extends Number, T, R extends List<T>> {
    private @java.lang.SuppressWarnings("all") java.util.concurrent.Callable<N> callable;
    private @java.lang.SuppressWarnings("all") boolean callable$set;
    private @java.lang.SuppressWarnings("all") T tee;
    private @java.lang.SuppressWarnings("all") boolean tee$set;
    private @java.lang.SuppressWarnings("all") R arrr;
    private @java.lang.SuppressWarnings("all") boolean arrr$set;
    @java.lang.SuppressWarnings("all") BuilderDefaultsGenericsBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenericsBuilder<N, T, R> callable(final java.util.concurrent.Callable<N> callable) {
      this.callable = callable;
      callable$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenericsBuilder<N, T, R> tee(final T tee) {
      this.tee = tee;
      tee$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenericsBuilder<N, T, R> arrr(final R arrr) {
      this.arrr = arrr;
      arrr$set = true;
      return this;
    }
    public @java.lang.SuppressWarnings("all") BuilderDefaultsGenerics<N, T, R> build() {
      return new BuilderDefaultsGenerics<N, T, R>((callable$set ? callable : BuilderDefaultsGenerics.<N, T, R>$default$callable()), (tee$set ? tee : BuilderDefaultsGenerics.<N, T, R>$default$tee()), (arrr$set ? arrr : BuilderDefaultsGenerics.<N, T, R>$default$arrr()));
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (((((("BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder(callable=" + this.callable) + ", tee=") + this.tee) + ", arrr=") + this.arrr) + ")");
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
  public static @java.lang.SuppressWarnings("all") <N extends Number, T, R extends List<T>>BuilderDefaultsGenericsBuilder<N, T, R> builder() {
    return new BuilderDefaultsGenericsBuilder<N, T, R>();
  }
}
