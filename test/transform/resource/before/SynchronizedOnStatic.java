class SynchronizedOnStatic<Z> {
	static class Inner {
		private static Object LCK = new Object[0];
		@lombok.Synchronized("LCK")
		public void foo() {
			System.out.println();
		}
	}
	class Inner2 {
		private Object LCK = new Object[0];
		@lombok.Synchronized("LCK")
		public void foo() {
			System.out.println();
		}
	}
}
