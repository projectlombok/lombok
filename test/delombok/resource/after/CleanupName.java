class Cleanup {
	void test() throws Exception {
		Object o = "Hello World!";
		try {
			System.out.println(o);
		} finally {
			o.toString();
		}
	}
}
