record WithOnRecordComponent(String a, String b) {
	@java.lang.SuppressWarnings("all")
	public WithOnRecordComponent withA(final String a) {
		return this.a == a ? this : new WithOnRecordComponent(a, this.b);
	}
}
