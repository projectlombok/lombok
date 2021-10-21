//version :9
import lombok.val;

public class ValInvalidParameter {
	public void val() {
		val a = a(new NonExistingClass());
		val b = a(a(new NonExistingClass()));
		val c = nonExisitingMethod(b(1));
		val d = nonExistingObject.nonExistingMethod();
		val e = b(1).nonExistingMethod();
		val f = 1 > 2 ? a(new NonExistingClass()) : a(new NonExistingClass());
		val g = b2(1);
		val h = b2(a("a"), a(null));
		val i = a(a(null));
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