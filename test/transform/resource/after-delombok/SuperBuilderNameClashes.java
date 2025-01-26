public class SuperBuilderNameClashes {
	public static class GenericsClash<B, C, C2> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class GenericsClashBuilder<B, C, C2, C3 extends SuperBuilderNameClashes.GenericsClash<B, C, C2>, B2 extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, C3, B2>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B2 self();
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C3 build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class GenericsClashBuilderImpl<B, C, C2> extends SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, SuperBuilderNameClashes.GenericsClash<B, C, C2>, SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private GenericsClashBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNameClashes.GenericsClash<B, C, C2> build() {
				return new SuperBuilderNameClashes.GenericsClash<B, C, C2>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected GenericsClash(final SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <B, C, C2> SuperBuilderNameClashes.GenericsClash.GenericsClashBuilder<B, C, C2, ?, ?> builder() {
			return new SuperBuilderNameClashes.GenericsClash.GenericsClashBuilderImpl<B, C, C2>();
		}
	}
	public static class B {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class BBuilder<C extends SuperBuilderNameClashes.B, B2 extends SuperBuilderNameClashes.B.BBuilder<C, B2>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B2 self();
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.B.BBuilder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class BBuilderImpl extends SuperBuilderNameClashes.B.BBuilder<SuperBuilderNameClashes.B, SuperBuilderNameClashes.B.BBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private BBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNameClashes.B.BBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNameClashes.B build() {
				return new SuperBuilderNameClashes.B(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected B(final SuperBuilderNameClashes.B.BBuilder<?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNameClashes.B.BBuilder<?, ?> builder() {
			return new SuperBuilderNameClashes.B.BBuilderImpl();
		}
	}
	public static class C2 {
	}
	public static class C {
		C2 c2;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class CBuilder<C3 extends SuperBuilderNameClashes.C, B extends SuperBuilderNameClashes.C.CBuilder<C3, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private C2 c2;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B c2(final C2 c2) {
				this.c2 = c2;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C3 build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.C.CBuilder(c2=" + this.c2 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class CBuilderImpl extends SuperBuilderNameClashes.C.CBuilder<SuperBuilderNameClashes.C, SuperBuilderNameClashes.C.CBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private CBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNameClashes.C.CBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNameClashes.C build() {
				return new SuperBuilderNameClashes.C(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected C(final SuperBuilderNameClashes.C.CBuilder<?, ?> b) {
			this.c2 = b.c2;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNameClashes.C.CBuilder<?, ?> builder() {
			return new SuperBuilderNameClashes.C.CBuilderImpl();
		}
	}
	interface B2 {
		interface B4<X> {
		}
	}
	interface B3<Y> {
	}
	public static class ExtendsClauseCollision extends B implements B2.B4<Object>, B3<Object> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ExtendsClauseCollisionBuilder<C extends SuperBuilderNameClashes.ExtendsClauseCollision, B4 extends SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<C, B4>> extends B.BBuilder<C, B4> {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B4 self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder(super=" + super.toString() + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ExtendsClauseCollisionBuilderImpl extends SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<SuperBuilderNameClashes.ExtendsClauseCollision, SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ExtendsClauseCollisionBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNameClashes.ExtendsClauseCollision build() {
				return new SuperBuilderNameClashes.ExtendsClauseCollision(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected ExtendsClauseCollision(final SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<?, ?> b) {
			super(b);
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilder<?, ?> builder() {
			return new SuperBuilderNameClashes.ExtendsClauseCollision.ExtendsClauseCollisionBuilderImpl();
		}
	}
}
