class SynchronizedOnStatic<Z> {
	static class Inner {
		private static Object LCK = new Object[0];
		public void foo() {
			synchronized (SynchronizedOnStatic.Inner.LCK) {
				System.out.println();
			}
		}
	}
	class Inner2 {
		private Object LCK = new Object[0];
		public void foo() {
			synchronized (this.LCK) {
				System.out.println();
			}
		}
	}
}