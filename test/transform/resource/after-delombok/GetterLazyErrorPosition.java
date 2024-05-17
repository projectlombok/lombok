class GetterLazyErrorPosition {
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField() {
		java.lang.Object $value = this.field.get();
		if ($value == null) {
			synchronized (this.field) {
				$value = this.field.get();
				if ($value == null) {
					final String actualValue = true ? "" : new ErrorPosition();
					$value = actualValue == null ? this.field : actualValue;
					this.field.set($value);
				}
			}
		}
		return (String) ($value == this.field ? null : $value);
	}
}
