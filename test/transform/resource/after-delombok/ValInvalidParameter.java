//version 7:8
public class ValInvalidParameter {
	public void val() {
		final java.lang.Object a = a(new NonExistingClass());
		final java.lang.Object b = a(a(new NonExistingClass()));
		final java.lang.Object c = nonExisitingMethod(b(1));
		final java.lang.Object d = nonExistingObject.nonExistingMethod();
		final java.lang.Object e = b(1).nonExistingMethod();
		final java.lang.Object f = 1 > 2 ? a(new NonExistingClass()) : a(new NonExistingClass());
		final java.lang.Object g = b2(1);
		final java.lang.Integer h = b2(a("a"), a(null));
		final int i = a(a(null));
	}

	public int a(String param) {
		return 0;
	}

	public int a(Integer param) {
		return 0;
	}

	public Integer b(int i) {
		return i;
	}

	public Integer b2(int i, int j) {
		return i;
	}
}