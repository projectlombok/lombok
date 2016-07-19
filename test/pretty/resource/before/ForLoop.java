public class ForLoop {

	public static void main(String[] args) {
		// before loop
		for (int i = 0; i < 10; i++) {
			// start of block
			System.out.println(i);
			// end of block
		}
		// after loop
	}

	{
		int i;
		for (i = 0; i < 10; i++) {
			System.out.println(i);
		}
	}
}
