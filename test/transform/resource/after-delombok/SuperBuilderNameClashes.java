public class SuperBuilderNameClashes {
	public static class GenericsClash<B, C, C2> {
		@java.lang.SuppressWarnings("all")
		public static abstract class GenericsClashBuilder<B, C, C2, C3 extends SuperBuilderNameClashes.GenericsClash<B, C, C2>, B2 extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, C3, B2>> {
			@java.lang.SuppressWarnings("all")
			protected abstract B2 self();
			@java.lang.SuppressWarnings("all")
			public abstract C3 build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class GenericsClashBuilderImpl<B, C, C2> extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, SuperBuilderNameClashes.GenericsClash<B, C, C2>, SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>> {
			@java.lang.SuppressWarnings("all")
			private GenericsClashBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderNameClashes.GenericsClash<B, C, C2> build() {
				return new SuperBuilderNameClashes.GenericsClash<B, C, C2>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected GenericsClash(final SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		public static <B, C, C2> SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> builder() {
			return new SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>();
		}
	}
	public static class B {
		@java.lang.SuppressWarnings("all")
		public static abstract class BBuilder<C extends SuperBuilderNameClashes.B, B2 extends SuperBuilderNameClashes.B.BBuilder<C, B2>> {
			@java.lang.SuppressWarnings("all")
			protected abstract B2 self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.B.BBuilder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class BBuilderImpl extends SuperBuilderNameClashes.B.BBuilder<SuperBuilderNameClashes.B, SuperBuilderNameClashes.B.BBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private BBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderNameClashes.B.BBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderNameClashes.B build() {
				return new SuperBuilderNameClashes.B(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected B(final SuperBuilderNameClashes.B.BBuilder<?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		public static SuperBuilderNameClashes.B.BBuilder<?, ?> builder() {
			return new SuperBuilderNameClashes.B.BBuilderImpl();
		}
	}
	public static class C2 {
	}
	public static class C {
		C2 c2;
		@java.lang.SuppressWarnings("all")
		public static abstract class CBuilder<C3 extends SuperBuilderNameClashes.C, B extends SuperBuilderNameClashes.C.CBuilder<C3, B>> {
			@java.lang.SuppressWarnings("all")
			private C2 c2;
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C3 build();
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public B c2(final C2 c2) {
				this.c2 = c2;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.C.CBuilder(c2=" + this.c2 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class CBuilderImpl extends SuperBuilderNameClashes.C.CBuilder<SuperBuilderNameClashes.C, SuperBuilderNameClashes.C.CBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private CBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderNameClashes.C.CBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderNameClashes.C build() {
				return new SuperBuilderNameClashes.C(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected C(final SuperBuilderNameClashes.C.CBuilder<?, ?> b) {
			this.c2 = b.c2;
		}
		@java.lang.SuppressWarnings("all")
		public static SuperBuilderNameClashes.C.CBuilder<?, ?> builder() {
			return new SuperBuilderNameClashes.C.CBuilderImpl();
		}
	}
}
