//version :9
import lombok.var;

public class VarInFor {
	public void enhancedFor() {
		int[] list = new int[] {1, 2};
		for (var shouldBeInt : list) {
			System.out.println(shouldBeInt);
			var shouldBeInt2 = shouldBeInt;
		}
	}
}