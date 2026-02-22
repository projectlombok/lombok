class CheckReturnValueOff {
	final int x;
	CheckReturnValueOff(int x) {
		this.x = x;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public CheckReturnValueOff withX(final int x) {
		return this.x == x ? this : new CheckReturnValueOff(x);
	}
}
