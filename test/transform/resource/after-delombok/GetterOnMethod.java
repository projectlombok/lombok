class GetterOnMethod {
	int i;
	int j;
	@Deprecated
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	public int getJ() {
		return this.j;
	}
}
class GetterOnClassOnMethod {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	@java.lang.SuppressWarnings("all")
	public int getJ() {
		return this.j;
	}
}
class GetterOnClassAndOnAField {
	int i;
	int j;
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	public int getJ() {
		return this.j;
	}
}