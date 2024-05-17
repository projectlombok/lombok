import java.util.List;
class BuilderWithToBuilder<T> {
	private String mOne;
	private String mTwo;
	private T foo;
	private List<T> bars;
	public static <K> K rrr(BuilderWithToBuilder<K> x) {
		return x.foo;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithToBuilder(final String one, final String two, final T foo, final List<T> bars) {
		this.mOne = one;
		this.mTwo = two;
		this.foo = foo;
		this.bars = bars;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithToBuilderBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String one;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String two;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T foo;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<T> bars;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithToBuilderBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> one(final String one) {
			this.one = one;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> two(final String two) {
			this.two = two;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> foo(final T foo) {
			this.foo = foo;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> bar(final T bar) {
			if (this.bars == null) this.bars = new java.util.ArrayList<T>();
			this.bars.add(bar);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> bars(final java.util.Collection<? extends T> bars) {
			if (bars == null) {
				throw new java.lang.NullPointerException("bars cannot be null");
			}
			if (this.bars == null) this.bars = new java.util.ArrayList<T>();
			this.bars.addAll(bars);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> clearBars() {
			if (this.bars != null) this.bars.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToBuilder<T> build() {
			java.util.List<T> bars;
			switch (this.bars == null ? 0 : this.bars.size()) {
			case 0: 
				bars = java.util.Collections.emptyList();
				break;
			case 1: 
				bars = java.util.Collections.singletonList(this.bars.get(0));
				break;
			default: 
				bars = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.bars));
			}
			return new BuilderWithToBuilder<T>(this.one, this.two, this.foo, bars);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithToBuilder.BuilderWithToBuilderBuilder(one=" + this.one + ", two=" + this.two + ", foo=" + this.foo + ", bars=" + this.bars + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> builder() {
		return new BuilderWithToBuilder.BuilderWithToBuilderBuilder<T>();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> toBuilder() {
		final T foo = BuilderWithToBuilder.<T>rrr(this);
		final BuilderWithToBuilder.BuilderWithToBuilderBuilder<T> builder = new BuilderWithToBuilder.BuilderWithToBuilderBuilder<T>().one(this.mOne).two(this.mTwo).foo(foo);
		if (this.bars != null) builder.bars(this.bars);
		return builder;
	}
}
class ConstructorWithToBuilder<T> {
	private String mOne;
	private String mTwo;
	private T foo;
	private com.google.common.collect.ImmutableList<T> bars;
	public ConstructorWithToBuilder(String mOne, T baz, com.google.common.collect.ImmutableList<T> bars) {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class ConstructorWithToBuilderBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String mOne;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T baz;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private com.google.common.collect.ImmutableList<T> bars;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		ConstructorWithToBuilderBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> mOne(final String mOne) {
			this.mOne = mOne;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> baz(final T baz) {
			this.baz = baz;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> bars(final com.google.common.collect.ImmutableList<T> bars) {
			this.bars = bars;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public ConstructorWithToBuilder<T> build() {
			return new ConstructorWithToBuilder<T>(this.mOne, this.baz, this.bars);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "ConstructorWithToBuilder.ConstructorWithToBuilderBuilder(mOne=" + this.mOne + ", baz=" + this.baz + ", bars=" + this.bars + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> builder() {
		return new ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T>();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T> toBuilder() {
		return new ConstructorWithToBuilder.ConstructorWithToBuilderBuilder<T>().mOne(this.mOne).baz(this.foo).bars(this.bars);
	}
}
class StaticMethodWithToBuilder<T> {
	private T foo;
	public StaticMethodWithToBuilder(T foo) {
		this.foo = foo;
	}
	public static <T> StaticMethodWithToBuilder<T> of(T foo) {
		return new StaticMethodWithToBuilder<T>(foo);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class StaticMethodWithToBuilderBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T foo;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		StaticMethodWithToBuilderBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> foo(final T foo) {
			this.foo = foo;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public StaticMethodWithToBuilder<T> build() {
			return StaticMethodWithToBuilder.<T>of(this.foo);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder(foo=" + this.foo + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> builder() {
		return new StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T>();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T> toBuilder() {
		return new StaticMethodWithToBuilder.StaticMethodWithToBuilderBuilder<T>().foo(this.foo);
	}
}
