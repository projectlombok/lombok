import java.util.*;
public class BuilderDefaultsGenerics<N extends Number, T, R extends List<T>> {
	private java.util.concurrent.Callable<N> callable;
	private T tee;
	private R arrr;
	@java.lang.SuppressWarnings("all")
	private static <N extends Number, T, R extends List<T>> java.util.concurrent.Callable<N> $default$callable() {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	private static <N extends Number, T, R extends List<T>> T $default$tee() {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	private static <N extends Number, T, R extends List<T>> R $default$arrr() {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	BuilderDefaultsGenerics(final java.util.concurrent.Callable<N> callable, final T tee, final R arrr) {
		this.callable = callable;
		this.tee = tee;
		this.arrr = arrr;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderDefaultsGenericsBuilder<N extends Number, T, R extends List<T>> {
		@java.lang.SuppressWarnings("all")
		private boolean callable$set;
		@java.lang.SuppressWarnings("all")
		private java.util.concurrent.Callable<N> callable$value;
		@java.lang.SuppressWarnings("all")
		private boolean tee$set;
		@java.lang.SuppressWarnings("all")
		private T tee$value;
		@java.lang.SuppressWarnings("all")
		private boolean arrr$set;
		@java.lang.SuppressWarnings("all")
		private R arrr$value;
		@java.lang.SuppressWarnings("all")
		BuilderDefaultsGenericsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> callable(final java.util.concurrent.Callable<N> callable) {
			this.callable$value = callable;
			callable$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> tee(final T tee) {
			this.tee$value = tee;
			tee$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> arrr(final R arrr) {
			this.arrr$value = arrr;
			arrr$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenerics<N, T, R> build() {
			java.util.concurrent.Callable<N> callable$value = this.callable$value;
			if (!this.callable$set) callable$value = BuilderDefaultsGenerics.<N, T, R>$default$callable();
			T tee$value = this.tee$value;
			if (!this.tee$set) tee$value = BuilderDefaultsGenerics.<N, T, R>$default$tee();
			R arrr$value = this.arrr$value;
			if (!this.arrr$set) arrr$value = BuilderDefaultsGenerics.<N, T, R>$default$arrr();
			return new BuilderDefaultsGenerics<N, T, R>(callable$value, tee$value, arrr$value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder(callable$value=" + this.callable$value + ", tee$value=" + this.tee$value + ", arrr$value=" + this.arrr$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <N extends Number, T, R extends List<T>> BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R> builder() {
		return new BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder<N, T, R>();
	}
}
