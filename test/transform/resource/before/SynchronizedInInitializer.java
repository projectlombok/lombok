//eclipse: verify diet
import lombok.Synchronized;

public class SynchronizedInInitializer {
	public static final Runnable SYNCHRONIZED = new Runnable() {
		@Override
		@Synchronized
		public void run() {
			System.out.println("test");
		}
	};
}