public class LockedInInitializer {
	public static final Runnable LOCKED = new Runnable() {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private final java.util.concurrent.locks.Lock $lock = new java.util.concurrent.locks.ReentrantLock();
		@Override
		public void run() {
			this.$lock.lock();
			try {
				System.out.println("test");
			} finally {
				this.$lock.unlock();
			}
		}
	};
	public static final Runnable LOCKED_READ = new Runnable() {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private final java.util.concurrent.locks.ReadWriteLock $lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
		@Override
		public void run() {
			this.$lock.readLock().lock();
			try {
				System.out.println("test");
			} finally {
				this.$lock.readLock().unlock();
			}
		}
	};
	public static final Runnable LOCKED_WRITE = new Runnable() {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private final java.util.concurrent.locks.ReadWriteLock $lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
		@Override
		public void run() {
			this.$lock.writeLock().lock();
			try {
				System.out.println("test");
			} finally {
				this.$lock.writeLock().unlock();
			}
		}
	};
}
