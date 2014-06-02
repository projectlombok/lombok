// version 7:
public class MultiCatch {
	public void test() {
		try {
			System.out.println();
		} catch (IllegalArgumentException | IllegalStateException e) {
			e.printStackTrace();
		}
	}
}