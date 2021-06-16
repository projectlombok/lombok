import java.io.IOException;

public class BuilderWithDefaultAndConstructorThrows {
	private final int value;

	public BuilderWithDefaultAndConstructorThrows(int value) throws IOException {
		if (value > 100) {
			throw new IOException("value too large");
		}
		this.value = value;
	}

	@java.lang.SuppressWarnings("all")
	private static int $default$value() {
		return 10;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithDefaultAndConstructorThrowsBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean value$set;
		@java.lang.SuppressWarnings("all")
		private int value$value;
		@java.lang.SuppressWarnings("all")
		private int value;
		@java.lang.SuppressWarnings("all")
		BuilderWithDefaultAndConstructorThrowsBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder value(final int value) {
			this.value$value = value;
			value$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithDefaultAndConstructorThrows build() throws IOException {
			int value$value = this.value$value;
			if (!this.value$set) value$value = BuilderWithDefaultAndConstructorThrows.$default$value();
			return new BuilderWithDefaultAndConstructorThrows(value$value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder(value$value=" + this.value$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder builder() {
		return new BuilderWithDefaultAndConstructorThrows.BuilderWithDefaultAndConstructorThrowsBuilder();
	}
}
