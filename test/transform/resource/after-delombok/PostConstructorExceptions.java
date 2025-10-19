class PostConstructorExceptions {
	private String a;

	PostConstructorExceptions() throws IllegalArgumentException, InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
		post();
		post2();
	}

	private void post() throws IllegalArgumentException {
	}

	private void post2() throws InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorExceptions(final String a) throws IllegalArgumentException, InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
		this.a = a;
		post();
		post2();
	}
}