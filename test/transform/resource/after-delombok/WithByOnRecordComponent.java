// version 14:
public record WithByOnRecordComponent(String a, String b) {
	@java.lang.SuppressWarnings("all")
	public WithByOnRecordComponent withABy(final java.util.function.Function<? super String, ? extends String> transformer) {
		return new WithByOnRecordComponent(transformer.apply(this.a), this.b);
	}
}
