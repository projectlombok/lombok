class Getter {
	boolean foo;
	boolean isBar;
	boolean hasBaz;
	@java.lang.SuppressWarnings("all")
	public boolean isFoo() {
		return this.foo;
	}
	@java.lang.SuppressWarnings("all")
	public boolean isBar() {
		return this.isBar;
	}
	@java.lang.SuppressWarnings("all")
	public boolean hasBaz() {
		return this.hasBaz;
	}
}
class MoreGetter {
	boolean foo;
	boolean hasFoo() {
		return true;
	}
}