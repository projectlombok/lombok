class PostConstructorGeneratedNoArgs {
	private String a;
	private String b;

	private void postConstructor() {
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorGeneratedNoArgs() {
		postConstructor();
	}
}

class PostConstructorGeneratedRequiredArgs {
	private final String a;
	private final String b;

	private void postConstructor() {
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorGeneratedRequiredArgs(final String a, final String b) {
		this.a = a;
		this.b = b;
		postConstructor();
	}
}

class PostConstructorGeneratedAllArgs {
	private String a;
	private String b;

	private void postConstructor() {
	}

	@java.lang.SuppressWarnings("all")
	public PostConstructorGeneratedAllArgs(final String a, final String b) {
		this.a = a;
		this.b = b;
		postConstructor();
	}
}
