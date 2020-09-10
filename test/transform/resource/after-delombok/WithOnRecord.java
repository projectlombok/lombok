record WithOnRecord(String a, String b) {
	@java.lang.SuppressWarnings("all")
	public WithOnRecord withA(final String a) {
		return this.a == a ? this : new WithOnRecord(a, this.b);
	}

	@java.lang.SuppressWarnings("all")
	public WithOnRecord withB(final String b) {
		return this.b == b ? this : new WithOnRecord(this.a, b);
	}
}
