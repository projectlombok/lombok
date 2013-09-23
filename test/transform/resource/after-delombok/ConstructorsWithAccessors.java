class ConstructorsWithAccessors {
	int plower;
	int pUpper;
	int _huh;
	int __huh2;

	@java.beans.ConstructorProperties({"plower", "upper", "huh", "_huh2"})
	@java.lang.SuppressWarnings("all")
	public ConstructorsWithAccessors(final int plower, final int upper, final int huh, final int _huh2) {
		this.plower = plower;
		this.pUpper = upper;
		this._huh = huh;
		this.__huh2 = _huh2;
	}
}
