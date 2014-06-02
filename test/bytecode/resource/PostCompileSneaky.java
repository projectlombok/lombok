public class PostCompileSneaky {
	public void test() {
		throw lombok.Lombok.sneakyThrow(new Exception());
	}
}