class WitherLegacyStar {
	int i;
	WitherLegacyStar(int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	public WitherLegacyStar withI(final int i) {
		return this.i == i ? this : new WitherLegacyStar(i);
	}
}
