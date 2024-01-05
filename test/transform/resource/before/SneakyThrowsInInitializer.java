//eclipse: verify diet
import lombok.SneakyThrows;

public class SneakyThrowsInInitializer {

	public static final Runnable R = new Runnable() {
		
		@Override
		@SneakyThrows
		public void run() {
			System.out.println("test");
			
		}
	};
}
