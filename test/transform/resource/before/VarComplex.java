import lombok.var;

public class VarComplex {
	private String field = "";
	private static final int CONSTANT = 20;

	public void testComplex() {
		var shouldBeCharArray = field.toCharArray();
		var shouldBeInt = CONSTANT;
		var lock = new Object();
		synchronized (lock) {
			var field = 20; //Shadowing
			var inner = 10;
			switch (field) {
				case 5:
					var shouldBeCharArray2 = shouldBeCharArray;
					var innerInner = inner;
			}
		}
		var shouldBeString = field; //Unshadowing
	}
}