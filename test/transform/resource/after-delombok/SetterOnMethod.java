class SetterOnMethod {
	int i;
	int j;
	@Deprecated
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	public void setJ(final int j) {
		this.j = j;
	}
}
class SetterOnClassOnMethod {
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
class SetterOnClassAndOnAField {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	public void setJ(final int j) {
		this.j = j;
	}
}