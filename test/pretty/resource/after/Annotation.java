@SuppressWarnings("all")
class Annotation {
	@Override
	public String toString() {
		return super.toString();
	}
	public void method(@SuppressWarnings("unused") int x) {
	}
	public void method2(@SuppressWarnings({"unused"}) int y) {
	}
	public void method3(@SuppressWarnings({"unused", "unchecked"}) int z) {
	}
}
