class SkipSuppressWarnings {
	private String field = "";
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field2 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	@lombok.Generated
	public String getField() {
		return this.field;
	}
	@java.lang.SuppressWarnings({"unchecked"})
	@lombok.Generated
	public String getField2() {
		java.lang.Object $value = this.field2.get();
		if ($value == null) {
			synchronized (this.field2) {
				$value = this.field2.get();
				if ($value == null) {
					final String actualValue = "";
					$value = actualValue == null ? this.field2 : actualValue;
					this.field2.set($value);
				}
			}
		}
		return (String) ($value == this.field2 ? null : $value);
	}
}
