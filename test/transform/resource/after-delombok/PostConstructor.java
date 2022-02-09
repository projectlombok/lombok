class PostConstructorTest {
	private String name;
	private final Integer start;
	private final Integer end;

	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorTest(final Integer start, final Integer end) {
		this.start = start;
		this.end = end;
		validate();
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorTest(final String name, final Integer start, final Integer end) {
		this.name = name;
		this.start = start;
		this.end = end;
		validate();
	}
}