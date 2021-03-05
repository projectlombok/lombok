//version 8:
//CONF: lombok.addNullAnnotations = checkerframework
import java.util.List;

class NullAnnotatedCheckerFrameworkSuperBuilder {

	public static class Parent {
		int x;
		int y;
		int z;
		List<String> names;

		@java.lang.SuppressWarnings("all")
		private static int $default$x() {
			return 5;
		}


		@java.lang.SuppressWarnings("all")
		public static abstract class ParentBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent, B extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			private boolean x$set;
			@java.lang.SuppressWarnings("all")
			private int x$value;
			@java.lang.SuppressWarnings("all")
			private int y;
			@java.lang.SuppressWarnings("all")
			private int z;
			@java.lang.SuppressWarnings("all")
			private java.util.ArrayList<String> names;

			@java.lang.SuppressWarnings("all")
			protected abstract B self();

			@java.lang.SuppressWarnings("all")
			public abstract C build();

			/**
			 * @return {@code this}.
			 */
			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B x(final int x) {
				this.x$value = x;
				x$set = true;
				return self();
			}

			/**
			 * @return {@code this}.
			 */
			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B y(final int y) {
				this.y = y;
				return self();
			}

			/**
			 * @return {@code this}.
			 */
			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B z(final int z) {
				this.z = z;
				return self();
			}

			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B name(final String name) {
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.add(name);
				return self();
			}

			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B names(final java.util.@org.checkerframework.checker.nullness.qual.NonNull Collection<? extends String> names) {
				if (names == null) {
					throw new java.lang.NullPointerException("names cannot be null");
				}
				if (this.names == null) this.names = new java.util.ArrayList<String>();
				this.names.addAll(names);
				return self();
			}

			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B clearNames() {
				if (this.names != null) this.names.clear();
				return self();
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
				return "NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder(x$value=" + this.x$value + ", y=" + this.y + ", z=" + this.z + ", names=" + this.names + ")";
			}
		}


		@java.lang.SuppressWarnings("all")
		private static final class ParentBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.Parent, NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl self() {
				return this;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public NullAnnotatedCheckerFrameworkSuperBuilder.@org.checkerframework.checker.nullness.qual.NonNull Parent build() {
				return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent(this);
			}
		}

		@java.lang.SuppressWarnings("all")
		protected Parent(final NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilder<?, ?> b) {
			if (b.x$set) this.x = b.x$value;
			 else this.x = NullAnnotatedCheckerFrameworkSuperBuilder.Parent.$default$x();
			this.y = b.y;
			this.z = b.z;
			java.util.List<String> names;
			switch (b.names == null ? 0 : b.names.size()) {
			case 0: 
				names = java.util.Collections.emptyList();
				break;
			case 1: 
				names = java.util.Collections.singletonList(b.names.get(0));
				break;
			default: 
				names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.names));
			}
			this.names = names;
		}

		@java.lang.SuppressWarnings("all")
		public static NullAnnotatedCheckerFrameworkSuperBuilder.Parent.@org.checkerframework.checker.nullness.qual.NonNull ParentBuilder<?, ?> builder() {
			return new NullAnnotatedCheckerFrameworkSuperBuilder.Parent.ParentBuilderImpl();
		}
	}


	public static class ZChild extends Parent {
		int a;
		int b;

		@java.lang.SuppressWarnings("all")
		private static int $default$a() {
			return 1;
		}


		@java.lang.SuppressWarnings("all")
		public static abstract class ZChildBuilder<C extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, B extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			private boolean a$set;
			@java.lang.SuppressWarnings("all")
			private int a$value;
			@java.lang.SuppressWarnings("all")
			private int b;

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();

			/**
			 * @return {@code this}.
			 */
			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B a(final int a) {
				this.a$value = a;
				a$set = true;
				return self();
			}

			/**
			 * @return {@code this}.
			 */
			@org.checkerframework.checker.nullness.qual.NonNull
			@java.lang.SuppressWarnings("all")
			public B b(final int b) {
				this.b = b;
				return self();
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.@org.checkerframework.checker.nullness.qual.NonNull String toString() {
				return "NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder(super=" + super.toString() + ", a$value=" + this.a$value + ", b=" + this.b + ")";
			}
		}


		@java.lang.SuppressWarnings("all")
		private static final class ZChildBuilderImpl extends NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<NullAnnotatedCheckerFrameworkSuperBuilder.ZChild, NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private ZChildBuilderImpl() {
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl self() {
				return this;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public NullAnnotatedCheckerFrameworkSuperBuilder.@org.checkerframework.checker.nullness.qual.NonNull ZChild build() {
				return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild(this);
			}
		}

		@java.lang.SuppressWarnings("all")
		protected ZChild(final NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilder<?, ?> b) {
			super(b);
			if (b.a$set) this.a = b.a$value;
			 else this.a = NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.$default$a();
			this.b = b.b;
		}

		@java.lang.SuppressWarnings("all")
		public static NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.@org.checkerframework.checker.nullness.qual.NonNull ZChildBuilder<?, ?> builder() {
			return new NullAnnotatedCheckerFrameworkSuperBuilder.ZChild.ZChildBuilderImpl();
		}
	}
}
