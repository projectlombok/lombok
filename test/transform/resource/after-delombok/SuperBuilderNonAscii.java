public class SuperBuilderNonAscii {
	public static class 부모 {
		Long a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class 부모Builder<C extends SuperBuilderNonAscii.부모, B extends SuperBuilderNonAscii.부모.부모Builder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private Long a;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B a(final Long a) {
				this.a = a;
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
				return "SuperBuilderNonAscii.부모.부모Builder(a=" + this.a + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class 부모BuilderImpl extends SuperBuilderNonAscii.부모.부모Builder<SuperBuilderNonAscii.부모, SuperBuilderNonAscii.부모.부모BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private 부모BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNonAscii.부모.부모BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNonAscii.부모 build() {
				return new SuperBuilderNonAscii.부모(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected 부모(final SuperBuilderNonAscii.부모.부모Builder<?, ?> b) {
			this.a = b.a;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNonAscii.부모.부모Builder<?, ?> builder() {
			return new SuperBuilderNonAscii.부모.부모BuilderImpl();
		}
	}
	public static class 자식 extends 부모 {
		String b;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class 자식Builder<C extends SuperBuilderNonAscii.자식, B extends SuperBuilderNonAscii.자식.자식Builder<C, B>> extends 부모.부모Builder<C, B> {
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
				return "SuperBuilderNonAscii.자식.자식Builder(super=" + super.toString() + ", b=" + this.b + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class 자식BuilderImpl extends SuperBuilderNonAscii.자식.자식Builder<SuperBuilderNonAscii.자식, SuperBuilderNonAscii.자식.자식BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private 자식BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderNonAscii.자식.자식BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderNonAscii.자식 build() {
				return new SuperBuilderNonAscii.자식(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected 자식(final SuperBuilderNonAscii.자식.자식Builder<?, ?> b) {
			super(b);
			this.b = b.b;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderNonAscii.자식.자식Builder<?, ?> builder() {
			return new SuperBuilderNonAscii.자식.자식BuilderImpl();
		}
	}
	public static void test() {
		자식 x = 자식.builder().b("").a(5L).build();
	}
}
