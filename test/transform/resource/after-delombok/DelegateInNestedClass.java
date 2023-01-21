class DelegateInNestedClass {
	void localClass() {

		class LocalClass {
			private final java.lang.Runnable field = null;

			@java.lang.SuppressWarnings("all")
			public void run() {
				this.field.run();
			}
		}
	}

	void anonymousClass() {
		Runnable r = new Runnable() {
			private final java.lang.Runnable field = null;
			@java.lang.SuppressWarnings("all")
			public void run() {
				this.field.run();
			}
		};
	}


	class InnerClass {
		private final java.lang.Runnable field = null;

		@java.lang.SuppressWarnings("all")
		public void run() {
			this.field.run();
		}
	}


	static class StaticClass {
		private final java.lang.Runnable field = null;

		@java.lang.SuppressWarnings("all")
		public void run() {
			this.field.run();
		}
	}
}
