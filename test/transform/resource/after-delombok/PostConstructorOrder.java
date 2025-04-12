class PostConstructorOrder {
	private void first() {
	}

	private void second() {
	}

	private void third() {
	}

	private void fourth() {
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorOrder() {
		first();
		second();
		third();
		fourth();
	}
}
