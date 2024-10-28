class MixGetterVal {
	private int x;
	public void m(int z) {
	}
	public void test() {
		final int y = x;
		m(y);
		final int a = getX();
		m(a);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getX() {
		return this.x;
	}
}
