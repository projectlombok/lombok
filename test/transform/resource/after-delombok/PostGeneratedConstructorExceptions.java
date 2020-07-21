class PostGeneratedConstructorExceptions {
	private String a;

	PostGeneratedConstructorExceptions() {
	}

	private void post() throws IllegalArgumentException {
	}

	private void post2() throws InterruptedException {
	}

	@java.lang.SuppressWarnings("all")
	public PostGeneratedConstructorExceptions(final String a) throws IllegalArgumentException, InterruptedException {
		this.a = a;
		post();
		post2();
	}
}