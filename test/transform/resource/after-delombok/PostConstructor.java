class PostConstructorTest {
	private String name;
	private final Integer start;
	private final Integer end;
	private Integer range;

	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}

	private void calculateRange() {
		range = end - start;
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorTest(final Integer start, final Integer end) {
		this.start = start;
		this.end = end;
		validate();
		calculateRange();
	}
}