// version 14:
import java.util.List;
public record BuilderSimpleOnRecord<T>(List<T> l, String a) {
	@java.lang.SuppressWarnings("all")
	protected static class BuilderSimpleOnRecordBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private List<T> l;
		@java.lang.SuppressWarnings("all")
		private String a;
		@java.lang.SuppressWarnings("all")
		BuilderSimpleOnRecordBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> l(final List<T> l) {
			this.l = l;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> a(final String a) {
			this.a = a;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSimpleOnRecord<T> build() {
			return new BuilderSimpleOnRecord<T>(this.l, this.a);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder(l=" + this.l + ", a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	protected static <T> BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T> builder() {
		return new BuilderSimpleOnRecord.BuilderSimpleOnRecordBuilder<T>();
	}
}
