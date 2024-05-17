//version 8:
class ConstructorsWithAccessors {
	int plower;
	int pUpper;
	int _huh;
	int __huh2;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorsWithAccessors(final int plower, final int upper, final int huh, final int _huh2) {
		this.plower = plower;
		this.pUpper = upper;
		this._huh = huh;
		this.__huh2 = _huh2;
	}
}
class ConstructorsWithAccessorsNonNull {
	@lombok.NonNull
	Integer plower;
	@lombok.NonNull
	Integer pUpper;
	@lombok.NonNull
	Integer _huh;
	@lombok.NonNull
	final Integer __huh2;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorsWithAccessorsNonNull(@lombok.NonNull final Integer plower, @lombok.NonNull final Integer upper, @lombok.NonNull final Integer huh, @lombok.NonNull final Integer _huh2) {
		if (plower == null) {
			throw new java.lang.NullPointerException("plower is marked non-null but is null");
		}
		if (upper == null) {
			throw new java.lang.NullPointerException("upper is marked non-null but is null");
		}
		if (huh == null) {
			throw new java.lang.NullPointerException("huh is marked non-null but is null");
		}
		if (_huh2 == null) {
			throw new java.lang.NullPointerException("_huh2 is marked non-null but is null");
		}
		this.plower = plower;
		this.pUpper = upper;
		this._huh = huh;
		this.__huh2 = _huh2;
	}
}
