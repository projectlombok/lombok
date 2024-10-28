// version 14:
public record WithOnRecordComponent(String a, String b) {
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithOnRecordComponent withA(final String a) {
		return this.a == a ? this : new WithOnRecordComponent(a, this.b);
	}
}
