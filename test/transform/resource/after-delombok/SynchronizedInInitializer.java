public class SynchronizedInInitializer {
	public static final Runnable SYNCHRONIZED = new Runnable() {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private final java.lang.Object $lock = new java.lang.Object[0];
		@Override
		public void run() {
			synchronized (this.$lock) {
				System.out.println("test");
			}
		}
	};
}
