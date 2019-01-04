class WitherAndSetterJavadoc {
	/**
	 * Some value.
	 */
	int i;
	/**
	 * Some other value.
	 */
	int j;
	WitherAndSetterJavadoc(int i, int j) {
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
	 */
	@java.lang.SuppressWarnings("all")
	public WitherAndSetterJavadoc withI(final int i) {
		return this.i == i ? this : new WitherAndSetterJavadoc(i, this.j);
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
	 * @param the new other value
	 */
	@java.lang.SuppressWarnings("all")
	public WitherAndSetterJavadoc withJ(final int j) {
		return this.j == j ? this : new WitherAndSetterJavadoc(this.i, j);
	}
}
