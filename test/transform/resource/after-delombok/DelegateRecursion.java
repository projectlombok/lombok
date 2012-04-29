class DelegateRecursionOuterMost {
	private final DelegateRecursionCenter center = new DelegateRecursionCenter();
	@java.lang.SuppressWarnings("all")
	public void innerMostMethod() {
		this.center.innerMostMethod();
	}
}
class DelegateRecursionCenter {
	private final DelegateRecursionInnerMost inner = new DelegateRecursionInnerMost();
	@java.lang.SuppressWarnings("all")
	public void innerMostMethod() {
		this.inner.innerMostMethod();
	}
}
class DelegateRecursionInnerMost {
	public void innerMostMethod() {
	}
}