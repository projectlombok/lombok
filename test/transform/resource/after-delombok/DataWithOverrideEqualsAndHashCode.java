class DataWithOverrideEqualsAndHashCode {

	class Data1 {
	}

	class Data2 extends Data1 {
		public int hashCode() {
			return 42;
		}

		public boolean equals(Object other) {
			return false;
		}

		@java.lang.SuppressWarnings("all")
		public Data2() {
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "DataWithOverrideEqualsAndHashCode.Data2()";
		}
	}
}
