class WithOnJavaBeansSpecCapitalization {
	int aField;

	WithOnJavaBeansSpecCapitalization(int aField) {
	}

	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOnJavaBeansSpecCapitalization withaField(final int aField) {
		return this.aField == aField ? this : new WithOnJavaBeansSpecCapitalization(aField);
	}
}

class WithOffJavaBeansSpecCapitalization {
	int aField;

	WithOffJavaBeansSpecCapitalization(int aField) {
	}

	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public WithOffJavaBeansSpecCapitalization withAField(final int aField) {
		return this.aField == aField ? this : new WithOffJavaBeansSpecCapitalization(aField);
	}
}
