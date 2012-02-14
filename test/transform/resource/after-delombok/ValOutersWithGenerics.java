import java.util.*;
public class ValOutersWithGenerics<Z> {
	class Inner {
	}
	public void testOutersWithGenerics() {
		final java.lang.String foo = "";
		List<Inner> list = new ArrayList<Inner>();
		final ValOutersWithGenerics<Z>.Inner elem = list.get(0);
	}
	public void testLocalClasses() {
		class Local<A> {
		}
		final Local<java.lang.String> q = new Local<String>();
	}
	static class SubClass extends ValOutersWithGenerics<String> {
		public void testSubClassOfOutersWithGenerics() {
			List<Inner> list = new ArrayList<Inner>();
			final ValOutersWithGenerics<java.lang.String>.Inner elem = list.get(0);
		}
	}
}
