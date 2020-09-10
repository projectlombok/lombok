record WithByOnRecord(String a, String b) {
	@java.lang.SuppressWarnings("all")
	public WithByOnRecord withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
		return new WithByOnRecord(transformer.apply(this.a), this.b);
	}
}
