public class SuperBuilderNameClashes {
	public static class GenericsClash<B, C, C2> {
		@java.lang.SuppressWarnings("all")
		public static abstract class GenericsClashBuilder<B, C, C2, C3 extends GenericsClash<B, C, C2>, B2 extends GenericsClashBuilder<B, C, C2, C3, B2>> {
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
		private static final class GenericsClashBuilderImpl<B, C, C2> extends GenericsClashBuilder<B, C, C2, GenericsClash<B, C, C2>, GenericsClashBuilderImpl<B, C, C2>> {
			@java.lang.SuppressWarnings("all")
			private GenericsClashBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected GenericsClashBuilderImpl<B, C, C2> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public GenericsClash<B, C, C2> build() {
				return new GenericsClash<B, C, C2>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected GenericsClash(final GenericsClashBuilder<B, C, C2, ?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		public static <B, C, C2> GenericsClashBuilder<B, C, C2, ?, ?> builder() {
			return new GenericsClashBuilderImpl<B, C, C2>();
		}
	}
	public static class B {
		@java.lang.SuppressWarnings("all")
		public static abstract class BBuilder<C extends B, B2 extends BBuilder<C, B2>> {
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
		private static final class BBuilderImpl extends BBuilder<B, BBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private BBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected BBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public B build() {
				return new B(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected B(final BBuilder<?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		public static BBuilder<?, ?> builder() {
			return new BBuilderImpl();
		}
	}
	public static class C2 {
	}
	public static class C {
		C2 c2;
		@java.lang.SuppressWarnings("all")
		public static abstract class CBuilder<C3 extends C, B extends CBuilder<C3, B>> {
			@java.lang.SuppressWarnings("all")
			private C2 c2;
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C3 build();
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
		private static final class CBuilderImpl extends CBuilder<C, CBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private CBuilderImpl() {
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected CBuilderImpl self() {
				return this;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public C build() {
				return new C(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected C(final CBuilder<?, ?> b) {
			this.c2 = b.c2;
		}
		@java.lang.SuppressWarnings("all")
		public static CBuilder<?, ?> builder() {
			return new CBuilderImpl();
		}
	}
}
