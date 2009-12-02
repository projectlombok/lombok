import lombok.SneakyThrows;
class SneakyThrowsPlain {
	@lombok.SneakyThrows public void test() {
		System.out.println("test1");
	}
	
	@SneakyThrows public void test2() {
		System.out.println("test2");
	}
}