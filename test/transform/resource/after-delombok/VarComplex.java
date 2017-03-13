public class VarComplex {
	private String field = "";
	private static final int CONSTANT = 20;
	public void testComplex() {
		char[] shouldBeCharArray = field.toCharArray();
		int shouldBeInt = CONSTANT;
		java.lang.Object lock = new Object();
		synchronized (lock) {
			int field = 20; //Shadowing
			int inner = 10;
			switch (field) {
			case 5: 
				char[] shouldBeCharArray2 = shouldBeCharArray;
				int innerInner = inner;
			
			}
		}
		java.lang.String shouldBeString = field; //Unshadowing
	}
}
