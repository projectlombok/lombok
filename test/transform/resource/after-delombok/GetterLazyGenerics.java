class GetterLazyGenerics<E> {
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field2 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	public static <E> E getAny() {
		return null;
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public E getField() {
		java.lang.Object $value = this.field.get();
		if ($value == null) {
			synchronized (this.field) {
				$value = this.field.get();
				if ($value == null) {
					final E actualValue = getAny();
					$value = actualValue == null ? this.field : actualValue;
					this.field.set($value);
				}
			}
		}
		return (E) ($value == this.field ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public long getField2() {
		java.lang.Object $value = this.field2.get();
		if ($value == null) {
			synchronized (this.field2) {
				$value = this.field2.get();
				if ($value == null) {
					final long actualValue = System.currentTimeMillis();
					$value = actualValue;
					this.field2.set($value);
				}
			}
		}
		return (java.lang.Long) $value;
	}
}
