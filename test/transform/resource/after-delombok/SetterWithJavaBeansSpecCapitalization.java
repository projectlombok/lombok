class SetterWithoutJavaBeansSpecCapitalization {
	int a;
	int aField;
	@java.lang.SuppressWarnings("all")
	public void setA(final int a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	public void setAField(final int aField) {
		this.aField = aField;
	}
}

class SetterWithJavaBeansSpecCapitalization {
	int a;
	int aField;
	@java.lang.SuppressWarnings("all")
	public void setA(final int a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	public void setaField(final int aField) {
		this.aField = aField;
	}
}


