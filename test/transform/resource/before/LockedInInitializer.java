//eclipse: verify diet
import lombok.Locked;

public class LockedInInitializer {
	public static final Runnable LOCKED = new Runnable() {
		@Override
		@Locked
		public void run() {
			System.out.println("test");
		}
	};
	public static final Runnable LOCKED_READ = new Runnable() {
		@Override
		@Locked.Read
		public void run() {
			System.out.println("test");
		}
	};
	public static final Runnable LOCKED_WRITE = new Runnable() {
		@Override
		@Locked.Write
		public void run() {
			System.out.println("test");
		}
	};
}