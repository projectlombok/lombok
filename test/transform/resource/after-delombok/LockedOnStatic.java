class LockedOnStatic<Z> {
	static class Inner {
		private static final java.util.concurrent.locks.Lock LCK = new java.util.concurrent.locks.ReentrantLock();
		public void foo() {
			LockedOnStatic.Inner.LCK.lock();
			try {
				System.out.println();
			} finally {
				LockedOnStatic.Inner.LCK.unlock();
			}
		}
	}
	class Inner2 {
		private final java.util.concurrent.locks.ReentrantLock LCK = new java.util.concurrent.locks.ReentrantLock();
		public void foo() {
			this.LCK.lock();
			try {
				System.out.println();
			} finally {
				this.LCK.unlock();
			}
		}
	}
}
