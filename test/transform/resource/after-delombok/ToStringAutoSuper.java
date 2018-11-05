class ToStringAutoSuperWithNoParent {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ToStringAutoSuperWithNoParent()";
	}
}

class ToStringAutoSuperWithParent extends ToStringAutoSuperWithNoParent {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ToStringAutoSuperWithParent(super=" + super.toString() + ")";
	}
}
