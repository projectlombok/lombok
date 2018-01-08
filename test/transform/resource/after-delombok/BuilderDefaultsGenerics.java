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
		private java.util.concurrent.Callable<N> callable;
		@java.lang.SuppressWarnings("all")
		private boolean tee$set;
		@java.lang.SuppressWarnings("all")
		private T tee;
		@java.lang.SuppressWarnings("all")
		private boolean arrr$set;
		@java.lang.SuppressWarnings("all")
		private R arrr;
		@java.lang.SuppressWarnings("all")
		BuilderDefaultsGenericsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenericsBuilder<N, T, R> callable(final java.util.concurrent.Callable<N> callable) {
			this.callable = callable;
			callable$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenericsBuilder<N, T, R> tee(final T tee) {
			this.tee = tee;
			tee$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenericsBuilder<N, T, R> arrr(final R arrr) {
			this.arrr = arrr;
			arrr$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsGenerics<N, T, R> build() {
			java.util.concurrent.Callable<N> callable = this.callable;
			if (!callable$set) callable = BuilderDefaultsGenerics.<N, T, R>$default$callable();
			T tee = this.tee;
			if (!tee$set) tee = BuilderDefaultsGenerics.<N, T, R>$default$tee();
			R arrr = this.arrr;
			if (!arrr$set) arrr = BuilderDefaultsGenerics.<N, T, R>$default$arrr();
			return new BuilderDefaultsGenerics<N, T, R>(callable, tee, arrr);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderDefaultsGenerics.BuilderDefaultsGenericsBuilder(callable=" + this.callable + ", tee=" + this.tee + ", arrr=" + this.arrr + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <N extends Number, T, R extends List<T>> BuilderDefaultsGenericsBuilder<N, T, R> builder() {
		return new BuilderDefaultsGenericsBuilder<N, T, R>();
	}
}
