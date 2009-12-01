class Cleanup {
	void test() throws Exception {
		@lombok.Cleanup("toString") Object o = "Hello World!";
		System.out.println(o);
	}
}