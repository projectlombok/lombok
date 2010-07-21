import java.io.IOException;
class SneakyThrowsSingle {
	public void test() {
		try {
			System.out.println("test1");
		} catch (Throwable $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}
	public void test2() {
		try {
			System.out.println("test2");
			throw new IOException();
		} catch (IOException $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}
	public void test3() {
		try {
			System.out.println("test3");
			throw new IOException();
		} catch (IOException $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}
}