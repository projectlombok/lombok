import lombok.Value;

public class BuilderDefaultsArray {
	int[] x;
	java.lang.String[][] y;

	@java.lang.SuppressWarnings("all")
	private static int[] $default$x() {
		return new int[] {1, 2};
	}

	@java.lang.SuppressWarnings("all")
	private static java.lang.String[][] $default$y() {
		return new java.lang.String[][] {};
	}

	@java.lang.SuppressWarnings("all")
	BuilderDefaultsArray(final int[] x, final java.lang.String[][] y) {
		this.x = x;
		this.y = y;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderDefaultsArrayBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int[] x$value;
		@java.lang.SuppressWarnings("all")
		private boolean y$set;
		@java.lang.SuppressWarnings("all")
		private java.lang.String[][] y$value;

		@java.lang.SuppressWarnings("all")
		BuilderDefaultsArrayBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsArray.BuilderDefaultsArrayBuilder x(final int[] x) {
			this.x$value = x;
			x$set = true;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsArray.BuilderDefaultsArrayBuilder y(final java.lang.String[][] y) {
			this.y$value = y;
			y$set = true;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsArray build() {
			int[] x$value = this.x$value;
			if (!this.x$set) x$value = BuilderDefaultsArray.$default$x();
			java.lang.String[][] y$value = this.y$value;
			if (!this.y$set) y$value = BuilderDefaultsArray.$default$y();
			return new BuilderDefaultsArray(x$value, y$value);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderDefaultsArray.BuilderDefaultsArrayBuilder(x$value=" + java.util.Arrays.toString(this.x$value) + ", y$value=" + java.util.Arrays.deepToString(this.y$value) + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderDefaultsArray.BuilderDefaultsArrayBuilder builder() {
		return new BuilderDefaultsArray.BuilderDefaultsArrayBuilder();
	}
}
