public class SneakyThrowsInInitializer {
	public static final Runnable R = new Runnable() {
		@Override
		public void run() {
			try {
				System.out.println("test");
			} catch (final java.lang.Throwable $ex) {
				throw lombok.Lombok.sneakyThrow($ex);
			}
		}
	};
}
