// version 8:
class GetterLazyArguments {
	static String fun() {
		return null;
	}
	static String stringInt(String arg1, Integer arg2) {
		return null;
	}
	static String stringRunnable(String arg1, Runnable arg2) {
		return null;
	}
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field1 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field2 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field3 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field4 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field5 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	private final java.util.concurrent.atomic.AtomicReference<java.lang.Object> field6 = new java.util.concurrent.atomic.AtomicReference<java.lang.Object>();
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField1() {
		java.lang.Object $value = this.field1.get();
		if ($value == null) {
			synchronized (this.field1) {
				$value = this.field1.get();
				if ($value == null) {
					final String actualValue = stringInt(("a"), (1));
					$value = actualValue == null ? this.field1 : actualValue;
					this.field1.set($value);
				}
			}
		}
		return (String) ($value == this.field1 ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField2() {
		java.lang.Object $value = this.field2.get();
		if ($value == null) {
			synchronized (this.field2) {
				$value = this.field2.get();
				if ($value == null) {
					final String actualValue = stringInt(true ? "a" : "b", true ? 1 : 0);
					$value = actualValue == null ? this.field2 : actualValue;
					this.field2.set($value);
				}
			}
		}
		return (String) ($value == this.field2 ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField3() {
		java.lang.Object $value = this.field3.get();
		if ($value == null) {
			synchronized (this.field3) {
				$value = this.field3.get();
				if ($value == null) {
					final String actualValue = stringInt(("a"), true ? 1 : 0);
					$value = actualValue == null ? this.field3 : actualValue;
					this.field3.set($value);
				}
			}
		}
		return (String) ($value == this.field3 ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField4() {
		java.lang.Object $value = this.field4.get();
		if ($value == null) {
			synchronized (this.field4) {
				$value = this.field4.get();
				if ($value == null) {
					final String actualValue = stringRunnable(fun(), () -> {
					});
					$value = actualValue == null ? this.field4 : actualValue;
					this.field4.set($value);
				}
			}
		}
		return (String) ($value == this.field4 ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField5() {
		java.lang.Object $value = this.field5.get();
		if ($value == null) {
			synchronized (this.field5) {
				$value = this.field5.get();
				if ($value == null) {
					final String actualValue = stringRunnable(("a"), () -> {
					});
					$value = actualValue == null ? this.field5 : actualValue;
					this.field5.set($value);
				}
			}
		}
		return (String) ($value == this.field5 ? null : $value);
	}
	@java.lang.SuppressWarnings({"all", "unchecked"})
	@lombok.Generated
	public String getField6() {
		java.lang.Object $value = this.field6.get();
		if ($value == null) {
			synchronized (this.field6) {
				$value = this.field6.get();
				if ($value == null) {
					final String actualValue = true ? stringInt(true ? "a" : "b", true ? 1 : 0) : "";
					$value = actualValue == null ? this.field6 : actualValue;
					this.field6.set($value);
				}
			}
		}
		return (String) ($value == this.field6 ? null : $value);
	}
}
