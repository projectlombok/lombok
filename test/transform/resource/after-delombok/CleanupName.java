class CleanupName {
	void test() {
		Object o = "Hello World!";
		try {
			System.out.println(o);
		} finally {
			if (o != null) {
				o.toString();
			}
		}
	}
	void test2() {
		Object o = "Hello World too!";
		try {
			System.out.println(o);
		} finally {
			if (o != null) {
				o.toString();
			}
		}
	}
}
