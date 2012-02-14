import java.util.*;
import lombok.val;

public class ValOutersWithGenerics<Z> {
	class Inner {
	}
	
	public void testOutersWithGenerics() {
		val foo = "";
		List<Inner> list = new ArrayList<Inner>();
		val elem = list.get(0);
	}
	
	public void testLocalClasses() {
		class Local<A> {}
		
		val q = new Local<String>();
	}
	
	static class SubClass extends ValOutersWithGenerics<String> {
		public void testSubClassOfOutersWithGenerics() {
			List<Inner> list = new ArrayList<Inner>();
			val elem = list.get(0);
		}
	}
}
