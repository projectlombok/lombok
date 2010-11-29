import lombok.val;

public class ValComplex {
	private ValSimple field = new ValSimple();
	private static final int CONSTANT = 20;
	
	public void testReferencingOtherFiles() {
		val shouldBeString = field.method();
		val shouldBeInt = CONSTANT;
		val lock = new Object();
		synchronized (lock) {
			val field = 20; //Shadowing
			val inner = 10;
			switch (field) {
				case 5:
					val shouldBeString2 = shouldBeString;
					val innerInner = inner;
			}
		}
		val shouldBeValSimple = field; //Unshadowing
	}
}