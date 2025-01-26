//skip-idempotent
@com.fasterxml.jackson.databind.annotation.JsonDeserialize
public class JacksonizedSuperBuilderWithJsonDeserialize {
	int field1;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static abstract class JacksonizedSuperBuilderWithJsonDeserializeBuilder<C extends JacksonizedSuperBuilderWithJsonDeserialize, B extends JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<C, B>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int field1;
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B field1(final int field1) {
			this.field1 = field1;
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
			return "JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder(field1=" + this.field1 + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl extends JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<JacksonizedSuperBuilderWithJsonDeserialize, JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedSuperBuilderWithJsonDeserialize build() {
			return new JacksonizedSuperBuilderWithJsonDeserialize(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected JacksonizedSuperBuilderWithJsonDeserialize(final JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<?, ?> b) {
		this.field1 = b.field1;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilder<?, ?> builder() {
		return new JacksonizedSuperBuilderWithJsonDeserialize.JacksonizedSuperBuilderWithJsonDeserializeBuilderImpl();
	}
}
