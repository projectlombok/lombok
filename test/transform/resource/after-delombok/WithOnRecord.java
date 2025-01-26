// version 14:
public record WithOnRecord(String a, String b) {
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithOnRecord withA(final String a) {
		return this.a == a ? this : new WithOnRecord(a, this.b);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithOnRecord withB(final String b) {
		return this.b == b ? this : new WithOnRecord(this.a, b);
	}
}
