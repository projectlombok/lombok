class PostConstructorExplicit {
	private void postConstructor1() {
	}
	private void postConstructor2() {
	}

	public PostConstructorExplicit(long a) {
		this();
	}

	public PostConstructorExplicit(int a) {
	}

	public PostConstructorExplicit(short a) {
		postConstructor1();
		postConstructor2();
	}

	public PostConstructorExplicit(byte a) {
	}
	
	public PostConstructorExplicit(boolean a) {
		this();
		postConstructor1();
		postConstructor2();
	}

	public PostConstructorExplicit(double a) {
		this();
	}

	public PostConstructorExplicit() {
	}
}
