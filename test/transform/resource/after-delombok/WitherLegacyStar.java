class WitherLegacyStar {
	int i;
	WitherLegacyStar(int i) {
		this.i = i;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WitherLegacyStar withI(final int i) {
		return this.i == i ? this : new WitherLegacyStar(i);
	}
}
