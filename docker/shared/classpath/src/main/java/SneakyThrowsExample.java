public class SneakyThrowsExample {
	@lombok.SneakyThrows
	public static void main(String... args) {
		throw new java.io.IOException("boo");
	}
}
