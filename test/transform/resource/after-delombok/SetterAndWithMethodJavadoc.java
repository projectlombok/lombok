class SetterAndWithMethodJavadoc {
	/**
	 * Some value.
	 */
	int i;
	/**
	 * Some other value.
	 */
	int j;
	SetterAndWithMethodJavadoc(int i, int j) {
		this.i = i;
		this.j = j;
	}
	/**
	 * Some value.
	 * @param the new value
	 */
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	/**
	 * Some value.
	 * @param the new value
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public SetterAndWithMethodJavadoc withI(final int i) {
		return this.i == i ? this : new SetterAndWithMethodJavadoc(i, this.j);
	}
	/**
	 * Set some other value.
	 * @param the new other value
	 */
	@java.lang.SuppressWarnings("all")
	public void setJ(final int j) {
		this.j = j;
	}
	/**
	 * Reinstantiate with some other value.
	 * @param the other new other value
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public SetterAndWithMethodJavadoc withJ(final int j) {
		return this.j == j ? this : new SetterAndWithMethodJavadoc(this.i, j);
	}
}
