public class ValComplex {
	private ValSimple field = new ValSimple();
	private static final int CONSTANT = 20;
	public void testReferencingOtherFiles() {
		final java.lang.String shouldBeString = field.method();
		final int shouldBeInt = CONSTANT;
		final java.lang.Object lock = new Object();
		synchronized (lock) {
			final int field = 20; //Shadowing
			final int inner = 10;
			switch (field) {
			case 5: 
				final java.lang.String shouldBeString2 = shouldBeString;
				final int innerInner = inner;
			
			}
		}
		final ValSimple shouldBeValSimple = field; //Unshadowing
	}
}