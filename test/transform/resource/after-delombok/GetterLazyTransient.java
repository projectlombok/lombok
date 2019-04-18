class GetterLazyTransient {
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> nonTransientField = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final transient int transientField = 2;
	private final transient int nonLazyTransientField = 3;
	@java.lang.SuppressWarnings("all")
	public int getNonTransientField() {
		java.lang.Object value = this.nonTransientField.get();
		if (value == null) {
			synchronized (this.nonTransientField) {
				value = this.nonTransientField.get();
				if (value == null) {
					final int actualValue = 1;
					value = actualValue;
					this.nonTransientField.set(value);
				}
			}
		}
		return (java.lang.Integer) value;
	}
	@java.lang.SuppressWarnings("all")
	public int getNonLazyTransientField() {
		return this.nonLazyTransientField;
	}
}
