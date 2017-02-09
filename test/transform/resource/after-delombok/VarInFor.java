public class VarInFor {
	public void enhancedFor() {
		int[] list = new int[] {1, 2};
		for (int shouldBeInt : list) {
			System.out.println(shouldBeInt);
			int shouldBeInt2 = shouldBeInt;
		}
	}
}
