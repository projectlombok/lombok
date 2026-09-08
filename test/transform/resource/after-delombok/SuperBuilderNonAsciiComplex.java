public class SuperBuilderNonAsciiComplex {
	public static class 부모<T> {
		Long a;
		T z;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class 부모Builder<T, C extends SuperBuilderNonAsciiComplex.부모<T>, B extends SuperBuilderNonAsciiComplex.부모.부모Builder<T, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private Long a;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private T z;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B a(final Long a) {
				this.a = a;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B z(final T z) {
				this.z = z;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNonAsciiComplex.부모.부모Builder(a=" + this.a + ", z=" + this.z + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class 부모BuilderImpl<T> extends SuperBuilderNonAsciiComplex.부모.부모Builder<T, SuperBuilderNonAsciiComplex.부모<T>, SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private 부모BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNonAsciiComplex.부모<T> build() {
				return new SuperBuilderNonAsciiComplex.부모<T>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected 부모(final SuperBuilderNonAsciiComplex.부모.부모Builder<T, ?, ?> b) {
			this.a = b.a;
			this.z = b.z;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <T> SuperBuilderNonAsciiComplex.부모.부모Builder<T, ?, ?> builder() {
			return new SuperBuilderNonAsciiComplex.부모.부모BuilderImpl<T>();
		}
	}
	public static class 자식 extends SuperBuilderNonAsciiComplex.부모<String> {
		String b;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class 자식Builder<C extends SuperBuilderNonAsciiComplex.자식, B extends SuperBuilderNonAsciiComplex.자식.자식Builder<C, B>> extends SuperBuilderNonAsciiComplex.부모.부모Builder<String, C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String b;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B b(final String b) {
				this.b = b;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderNonAsciiComplex.자식.자식Builder(super=" + super.toString() + ", b=" + this.b + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class 자식BuilderImpl extends SuperBuilderNonAsciiComplex.자식.자식Builder<SuperBuilderNonAsciiComplex.자식, SuperBuilderNonAsciiComplex.자식.자식BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private 자식BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNonAsciiComplex.자식.자식BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNonAsciiComplex.자식 build() {
				return new SuperBuilderNonAsciiComplex.자식(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected 자식(final SuperBuilderNonAsciiComplex.자식.자식Builder<?, ?> b) {
			super(b);
			this.b = b.b;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNonAsciiComplex.자식.자식Builder<?, ?> builder() {
			return new SuperBuilderNonAsciiComplex.자식.자식BuilderImpl();
		}
	}
	public static void test() {
		부모<String> x = 자식.builder().b("").a(5L).build();
	}
}