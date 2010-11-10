class SetterOnParam {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public void setI(@SuppressWarnings("all") final int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	public void setJ(@java.lang.SuppressWarnings("all") final int j) {
		this.j = j;
	}
}
class SetterOnClassOnParam {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	public void setJ(final int j) {
		this.j = j;
	}
}
class SetterOnClassAndOnAFieldParam {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	public void setJ(@java.lang.SuppressWarnings("all") final int j) {
		this.j = j;
	}
}