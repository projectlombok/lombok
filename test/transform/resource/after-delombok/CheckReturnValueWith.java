class CheckReturnValueWith {
	final int x;
	final String name;
	CheckReturnValueWith(int x, String name) {
		this.x = x;
		this.name = name;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@lombok.CheckReturnValue
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public CheckReturnValueWith withX(final int x) {
		return this.x == x ? this : new CheckReturnValueWith(x, this.name);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@lombok.CheckReturnValue
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public CheckReturnValueWith withName(final String name) {
		return this.name == name ? this : new CheckReturnValueWith(this.x, name);
	}
}
