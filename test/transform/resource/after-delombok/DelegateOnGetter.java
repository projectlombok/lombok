class DelegateOnGetter {
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> bar = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private interface Bar {
		void setList(java.util.ArrayList<java.lang.String> list);
		int getInt();
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public Bar getBar() {
		java.lang.Object $value = this.bar.get();
		if ($value == null) {
			synchronized (this.bar) {
				$value = this.bar.get();
				if ($value == null) {
					final Bar actualValue = new Bar() {
						public void setList(java.util.ArrayList<String> list) {
						}
						public int getInt() {
							return 42;
						}
					};
					$value = actualValue == null ? this.bar : actualValue;
					this.bar.set($value);
				}
			}
		}
		return (Bar) ($value == this.bar ? null : $value);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setList(final java.util.ArrayList<java.lang.String> list) {
		this.getBar().setList(list);
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getInt() {
		return this.getBar().getInt();
	}
}
