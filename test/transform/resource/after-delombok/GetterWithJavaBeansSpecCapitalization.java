class GetterWithoutJavaBeansSpecCapitalization {
	int a;
	int aField;
	@java.lang.SuppressWarnings("all")
	public int getA() {
		return this.a;
	}
	@java.lang.SuppressWarnings("all")
	public int getAField() {
		return this.aField;
	}
}


class GetterWithJavaBeansSpecCapitalization {
	int a;
	int aField;
	@java.lang.SuppressWarnings("all")
	public int getA() {
		return this.a;
	}
	@java.lang.SuppressWarnings("all")
	public int getaField() {
		return this.aField;
	}
}


